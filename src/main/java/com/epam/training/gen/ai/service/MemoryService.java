package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookChunk;
import com.epam.training.gen.ai.dto.SearchInfoDTO;
import com.epam.training.gen.ai.embedding.EmbeddingService;
import com.epam.training.gen.ai.service.util.SKUtils;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.DefaultPromptTemplate;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.InputVariable;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryService {
    private static final String INIT_DIR = "src/main/resources/init";
    private static final int CHUNK_WORD_COUNT = 800;

    private final PDFTextStripper pdfStripper;
    private final EmbeddingService embeddingService;
    @Qualifier("bookAssistantMemoryPrompt")
    private final String promptTemplateText;

    private final Kernel kernel;
    @Qualifier("bookAssistantInvocationContext")
    private final InvocationContext invocationContext;

    private final ChatCompletionService chatCompletionService;

    @PostConstruct
    public void init() {
        var fileNames = getAllFiles();

        rememberBooks(fileNames);
    }

    public void readBook(String bookPath) {
        rememberBooks(List.of(bookPath));
    }

    public String answer(String input) {
        var answerFromMemory = embeddingService.search(input, BookChunk.class);

        var context = answerFromMemory.stream()
                .map(SearchInfoDTO::getPayload)
                .map(BookChunk::getChunk)
                .collect(Collectors.toSet());

        var prompt = buildPrompt(input, context);

        var contextsList = chatCompletionService.getChatMessageContentsAsync(prompt, kernel, invocationContext).block();

        var assistantResponse = SKUtils.extractAssistantResponse(contextsList);
        log.info(assistantResponse);

        return assistantResponse;
    }

    private void rememberBooks(List<String> fileNames) {
        var start = System.currentTimeMillis();
        Flux.fromIterable(fileNames)
                .map(this::readBookAndPrepareChunks)
                .flatMap(Flux::fromIterable)
                .publishOn(Schedulers.boundedElastic())
                .map(bookChunk -> embeddingService.save(bookChunk.getChunk(), bookChunk, BookChunk.class))
                .doOnNext(System.out::println)
                .blockLast();
        var end = System.currentTimeMillis();
        log.info("Reading of the books ({}) has finished. Time spent: {}", fileNames, (end - start));
    }

    private List<BookChunk> readBookAndPrepareChunks(String bookPath) {
        var path = Paths.get(bookPath);
        var source = path.getFileName().toString();
        log.info("Reading {}", source);

        var bookText = readPdf(bookPath);
        var bookChunks = splitIntoChunks(bookText);

        return bookChunks.stream()
                .map(bookChunk -> convertToBookChunk(source, bookChunk))
                .toList();
    }

    private String buildPrompt(String input, Set<String> context) {
        var config = PromptTemplateConfig.defaultTemplateBuilder()
                .addInputVariable(InputVariable.build("question", String.class, "The user's question to answer", null, null, true))
                .addInputVariable(InputVariable.build("context", List.class, "Retrieved context chunks from knowledge base", null, null, true))
                .withTemplate(promptTemplateText)
                .build();

        var template = DefaultPromptTemplate.build(config);

        var arguments = KernelFunctionArguments.builder()
                .withVariable("question", input)
                .withVariable("context", context)
                .build();

        var prompt = template.renderAsync(kernel, arguments, invocationContext).block();
        return prompt;
    }

    private List<String> getAllFiles() {
        try (Stream<Path> fileStream = Files.walk(Paths.get(INIT_DIR))) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to process directory: " + MemoryService.INIT_DIR, e);
        }
    }

    private String readPdf(String filePath) {
        String result = Strings.EMPTY;

        try (var document = Loader.loadPDF(new File(filePath))) {
            if (!document.isEncrypted()) {
                result = pdfStripper.getText(document);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private List<String> splitIntoChunks(String text) {
        String[] words = text.split("\\s+");

        return IntStream.range(0, (words.length + CHUNK_WORD_COUNT - 1) / CHUNK_WORD_COUNT)
                .mapToObj(i ->
                        Arrays.stream(words, i * CHUNK_WORD_COUNT, Math.min((i + 1) * CHUNK_WORD_COUNT, words.length))
                                .collect(Collectors.joining(" "))
                )
                .collect(Collectors.toList());
    }

    private BookChunk convertToBookChunk(String source, String chunk) {
        return BookChunk.builder()
                .chunk(chunk)
                .source(source)
                .build();
    }
}

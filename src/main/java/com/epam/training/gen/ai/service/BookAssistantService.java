package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import com.epam.training.gen.ai.service.util.SKUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookAssistantService {

    public static final String JSON_RESPONSE_RESTRICTION = " (RESPONSE FORMAT FOR THIS PROMPT: JSON)";

    private final ChatCompletionService chatCompletionService;
    @Qualifier("googleChatCompletionService")
    private final ChatCompletionService googleChatCompletionService;

    private final Kernel kernel;
    @Qualifier("googleKernel")
    private final Kernel googleKernel;

    private final ObjectMapper objectMapper;

    @Qualifier("bookAssistantInvocationContext")
    private final InvocationContext invocationContext;
    @Qualifier("bookAssistantInvocationContextForBookInfo")
    private final InvocationContext invocationContextForBookInfo;
    @Qualifier("bookAssistantChatHistory")
    private final ChatHistory chatHistory;


    public String processInput(String input) {
        chatHistory.addUserMessage(input);
        var contextsList = chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContext).block();

        var assistantResponse = SKUtils.extractAssistantResponse(contextsList);
        log.info(assistantResponse);
        chatHistory.addAssistantMessage(assistantResponse);

        return assistantResponse;
    }

    /***
     * Please ignore the next method as {@link ChatCompletionService} and {@link Kernel} for gemini are not configured properly in this application
     * This is an example of the component to use anyway
     */
    public String processInputGoogle(String input) {
        chatHistory.addUserMessage(input);
        var contextsList = googleChatCompletionService.getChatMessageContentsAsync(chatHistory, googleKernel, invocationContext).block();

        var assistantResponse = SKUtils.extractAssistantResponse(contextsList);
        log.info(assistantResponse);
        chatHistory.addAssistantMessage(assistantResponse);

        return assistantResponse;
    }

    public BookInfo processBook(String input) {
        chatHistory.addUserMessage(modifyInput(input));
        var contextsList = chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContextForBookInfo).block();

        var assistantResponse = SKUtils.extractAssistantResponse(contextsList);
        log.info(assistantResponse);
        chatHistory.addAssistantMessage(assistantResponse);

        BookInfo result;
        try {
            result = objectMapper.readValue(assistantResponse, BookInfo.class);
        } catch (JsonProcessingException e) {
            log.error("JSON converter error", e);
            throw new ServiceWorkException(String.format("Cannot convert AI response to the BookInfo. AI response: (%s)", assistantResponse), e);
        }

        return result;
    }

    private String modifyInput(String input) {
        return input.concat(JSON_RESPONSE_RESTRICTION);
    }
}

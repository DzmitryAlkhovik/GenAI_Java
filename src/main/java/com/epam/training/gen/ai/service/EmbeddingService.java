package com.epam.training.gen.ai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.dto.SearchBookInfoDTO;
import com.epam.training.gen.ai.exception.EmbeddingServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {
    private static final String COLLECTION_NAME = "book_collection";
    private static final String INFO_KEY = "info";

    private final OpenAIAsyncClient openAIAsyncClient;
    private final QdrantClient qdrantClient;
    private final ObjectMapper objectMapper;

    @Value("${client.embedding.model.name}")
    private final String embeddingModelName;

    @PostConstruct
    public void initCollection() {
        setUpCollection();
    }

    public UpdateStatus save(String input, BookInfo bookInfo) {
        var embeddingItems = retrieveVector(input);

        var vectors = mergeEmbeddingsValues(embeddingItems);

        var point = PointStruct.newBuilder()
                .setId(PointIdFactory.id(UUID.randomUUID()))
                .setVectors(VectorsFactory.vectors(vectors))
                .putPayload(INFO_KEY, convertToValue(bookInfo))
                .build();

        var request = UpsertPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addPoints(point)
                .build();

        try {
            return qdrantClient.upsertAsync(request).get().getStatus();
        } catch (InterruptedException | ExecutionException e) {
            throw new EmbeddingServiceException(e);
        }
    }

    public List<SearchBookInfoDTO> search(String input) {
        var inputVector = retrieveVector(input);

        //TODO: should throw exception on non-one item response???
        log.info("There are ({}) objects in model response", inputVector.size());

        var allVector = mergeEmbeddingsValues(inputVector);

        var request = SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addAllVector(allVector)
                .setWithPayload(WithPayloadSelectorFactory.enable(true))
                .setLimit(3)
                .build();

        try {
            return qdrantClient.searchAsync(request).get().stream()
                    .map(this::convertToBookInfo)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new EmbeddingServiceException(e);
        }
    }

    private void setUpCollection() {
        try {
            var alreadyExist = qdrantClient.collectionExistsAsync(COLLECTION_NAME).get();

            if (alreadyExist) {
                log.info("Collection [{}] already exists. Recreating...", COLLECTION_NAME);
                var deleteResult = qdrantClient.deleteCollectionAsync(COLLECTION_NAME);
                log.info("Collection [{}] deleting result: [{}]", COLLECTION_NAME, deleteResult.get().getResult());
            }

            createCollection();
        } catch (ExecutionException | InterruptedException e) {
            throw new EmbeddingServiceException("createCollection() issue", e);
        }
    }

    private void createCollection() {
        try {
            var result = qdrantClient.createCollectionAsync(COLLECTION_NAME,
                            Collections.VectorParams.newBuilder()
                                    .setDistance(Collections.Distance.Cosine)
                                    .setSize(1536)
                                    .build())
                    .get();
            log.info("Collection [{}] creation result: [{}]", COLLECTION_NAME, result.getResult());
        } catch (ExecutionException | InterruptedException e) {
            throw new EmbeddingServiceException("createCollection() issue", e);
        }
    }

    public List<EmbeddingItem> retrieveVector(String input) {
        return retrieveEmbeddings(input).getData();
    }

    private JsonWithInt.Value convertToValue(BookInfo bookInfo) {

        String objectAsString;
        try {
            objectAsString = objectMapper.writeValueAsString(bookInfo);
        } catch (JsonProcessingException e) {
            throw new EmbeddingServiceException("payload cannot be converted to string", e);
        }

        return ValueFactory.value(objectAsString);
    }

    private SearchBookInfoDTO convertToBookInfo(ScoredPoint point) {
        var id = point.getId().getUuid();
        var score = point.getScore();

        var dtoBuilder = SearchBookInfoDTO.builder()
                .id(id)
                .score(score);

        var value = point.getPayloadMap().get(INFO_KEY);
        if (Objects.nonNull(value) && value.hasStringValue()) {
            var bookInfo = convertToBookInfo(value.getStringValue());
            dtoBuilder.bookInfo(bookInfo);
        }

        return dtoBuilder.build();
    }

    private BookInfo convertToBookInfo(String objectAsString) {
        BookInfo result;
        try {
            result = objectMapper.readValue(objectAsString, BookInfo.class);
        } catch (JsonProcessingException e) {
            throw new EmbeddingServiceException("payload cannot be converted to BookInfo", e);
        }

        return result;
    }

    private List<Float> mergeEmbeddingsValues(List<EmbeddingItem> embeddingItems) {
        return embeddingItems.stream()
                .flatMap(embeddingItem -> embeddingItem.getEmbedding().stream())
                .toList();
    }

    private Embeddings retrieveEmbeddings(String input) {
        var qembeddingsOptions = new EmbeddingsOptions(List.of(input));
        return openAIAsyncClient
                .getEmbeddings(embeddingModelName, qembeddingsOptions)
                .block();
    }
}

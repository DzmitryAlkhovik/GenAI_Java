package com.epam.training.gen.ai.embedding;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.training.gen.ai.dto.SearchInfoDTO;
import com.epam.training.gen.ai.exception.EmbeddingServiceException;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Points.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {
    private static final String INFO_KEY = "info";

    private final EmbeddingHandlerRegistry handlerRegistry;
    private final OpenAIAsyncClient openAIAsyncClient;
    private final QdrantClient qdrantClient;

    @Value("${client.embedding.model.name}")
    private final String embeddingModelName;

    public <T> UpdateStatus save(String input, T objectInfo, Class<T> classInfo) {
        EmbeddingHandler<T> handler = handlerRegistry.getHandler(classInfo);

        var embeddingItems = retrieveVector(input);

        var vectors = mergeEmbeddingsValues(embeddingItems);

        var point = PointStruct.newBuilder()
                .setId(PointIdFactory.id(UUID.randomUUID()))
                .setVectors(VectorsFactory.vectors(vectors))
                .putPayload(INFO_KEY, handler.convertToValue(objectInfo))
                .build();

        var request = UpsertPoints.newBuilder()
                .setCollectionName(handler.getCollectionName())
                .addPoints(point)
                .build();

        try {
            return qdrantClient.upsertAsync(request).get().getStatus();
        } catch (InterruptedException | ExecutionException e) {
            throw new EmbeddingServiceException(e);
        }
    }

    public <T> List<SearchInfoDTO<T>> search(String input, Class<T> classInfo) {
        EmbeddingHandler<T> handler = handlerRegistry.getHandler(classInfo);

        var inputVector = retrieveVector(input);

        //TODO: should throw exception on non-one item response???
        log.info("There are ({}) objects in model response", inputVector.size());

        var allVector = mergeEmbeddingsValues(inputVector);

        var request = SearchPoints.newBuilder()
                .setCollectionName(handler.getCollectionName())
                .addAllVector(allVector)
                .setWithPayload(WithPayloadSelectorFactory.enable(true))
                .setLimit(3)
                .build();

        try {
            return qdrantClient.searchAsync(request).get().stream()
                    .map(scoredPoint -> convertToSearchInfo(scoredPoint, handler::convertToObject))
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new EmbeddingServiceException(e);
        }
    }

    public List<EmbeddingItem> retrieveVector(String input) {
        return retrieveEmbeddings(input).getData();
    }

    private <T> SearchInfoDTO<T> convertToSearchInfo(ScoredPoint point, Function<String, T> payloadConverter) {
        var id = point.getId().getUuid();
        var score = point.getScore();

        var dtoBuilder = SearchInfoDTO.<T>builder()
                .id(id)
                .score(score);

        var value = point.getPayloadMap().get(INFO_KEY);
        if (Objects.nonNull(value) && value.hasStringValue()) {
            var payload = payloadConverter.apply(value.getStringValue());
            dtoBuilder.payload(payload);
        }

        return dtoBuilder.build();
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

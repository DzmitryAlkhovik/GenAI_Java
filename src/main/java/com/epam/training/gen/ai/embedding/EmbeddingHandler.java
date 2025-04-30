package com.epam.training.gen.ai.embedding;

import com.epam.training.gen.ai.exception.EmbeddingServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
public abstract class EmbeddingHandler<T> {
    private final ObjectMapper objectMapper;
    private final QdrantClient qdrantClient;

    protected abstract String getCollectionName();

    protected abstract Class<T> getTypeClass();

    public JsonWithInt.Value convertToValue(T object) {
        String objectAsString;
        try {
            objectAsString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EmbeddingServiceException("payload cannot be converted to string", e);
        }

        return ValueFactory.value(objectAsString);
    }

    public T convertToObject(String objectAsString) {
        T result;
        try {
            result = objectMapper.readValue(objectAsString, getTypeClass());
        } catch (JsonProcessingException e) {
            throw new EmbeddingServiceException("payload cannot be converted to Object", e);
        }

        return result;
    }

    public void setUpCollection() {
        try {
            var alreadyExist = qdrantClient.collectionExistsAsync(getCollectionName()).get();

            if (alreadyExist) {
                log.info("Collection [{}] already exists. Recreating...", getCollectionName());
                var deleteResult = qdrantClient.deleteCollectionAsync(getCollectionName());
                log.info("Collection [{}] deleting result: [{}]", getCollectionName(), deleteResult.get().getResult());
            }

            createCollection();
        } catch (ExecutionException | InterruptedException e) {
            throw new EmbeddingServiceException("createCollection() issue", e);
        }
    }

    public void createCollection() {
        try {
            var result = qdrantClient.createCollectionAsync(getCollectionName(),
                            Collections.VectorParams.newBuilder()
                                    .setDistance(Collections.Distance.Cosine)
                                    .setSize(1536)
                                    .build())
                    .get();
            log.info("Collection [{}] creation result: [{}]", getCollectionName(), result.getResult());
        } catch (ExecutionException | InterruptedException e) {
            throw new EmbeddingServiceException("createCollection() issue", e);
        }
    }
}

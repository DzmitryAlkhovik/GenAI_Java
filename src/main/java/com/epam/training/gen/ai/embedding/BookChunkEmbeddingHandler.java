package com.epam.training.gen.ai.embedding;

import com.epam.training.gen.ai.dto.BookChunk;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookChunkEmbeddingHandler extends EmbeddingHandler<BookChunk> {

    @Autowired
    public BookChunkEmbeddingHandler(ObjectMapper objectMapper, QdrantClient qdrantClient) {
        super(objectMapper, qdrantClient);
    }

    @PostConstruct
    public void initCollection() {
        setUpCollection();
    }

    @Override
    public String getCollectionName() {
        return "memory_collection";
    }

    @Override
    public Class<BookChunk> getTypeClass() {
        return BookChunk.class;
    }
}

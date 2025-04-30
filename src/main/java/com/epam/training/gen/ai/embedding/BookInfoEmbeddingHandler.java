package com.epam.training.gen.ai.embedding;

import com.epam.training.gen.ai.dto.BookInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookInfoEmbeddingHandler extends EmbeddingHandler<BookInfo> {

    @Autowired
    public BookInfoEmbeddingHandler(ObjectMapper objectMapper, QdrantClient qdrantClient) {
        super(objectMapper, qdrantClient);
    }

    @PostConstruct
    public void initCollection() {
        setUpCollection();
    }

    @Override
    public String getCollectionName() {
        return "book_collection";
    }

    @Override
    public Class<BookInfo> getTypeClass() {
        return BookInfo.class;
    }
}

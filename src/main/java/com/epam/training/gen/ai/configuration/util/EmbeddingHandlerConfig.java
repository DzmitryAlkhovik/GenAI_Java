package com.epam.training.gen.ai.configuration.util;

import com.epam.training.gen.ai.dto.BookChunk;
import com.epam.training.gen.ai.dto.BookInfo;
import com.epam.training.gen.ai.embedding.BookChunkEmbeddingHandler;
import com.epam.training.gen.ai.embedding.BookInfoEmbeddingHandler;
import com.epam.training.gen.ai.embedding.EmbeddingHandlerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmbeddingHandlerConfig {
    private final EmbeddingHandlerRegistry registry;
    private final BookInfoEmbeddingHandler bookInfoEmbeddingHandler;
    private final BookChunkEmbeddingHandler bookChunkEmbeddingHandler;

    @PostConstruct
    public void init() {
        registry.registerHandler(BookInfo.class, bookInfoEmbeddingHandler);
        registry.registerHandler(BookChunk.class, bookChunkEmbeddingHandler);
    }
}

package com.epam.training.gen.ai.embedding;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmbeddingHandlerRegistry {
    private final Map<Class<?>, EmbeddingHandler<?>> handlers = new HashMap<>();

    public <T> void registerHandler(Class<T> type, EmbeddingHandler<T> handler) {
        handlers.put(type, handler);
    }

    public <T> EmbeddingHandler<T> getHandler(Class<T> type) {
        @SuppressWarnings("unchecked")
        EmbeddingHandler<T> handler = (EmbeddingHandler<T>) handlers.get(type);
        if (handler == null) {
            throw new RuntimeException("No handler found for type: " + type.getName());
        }
        return handler;
    }
}

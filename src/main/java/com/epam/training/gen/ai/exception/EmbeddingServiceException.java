package com.epam.training.gen.ai.exception;

public class EmbeddingServiceException extends RuntimeException {
    public EmbeddingServiceException() {
        super();
    }

    public EmbeddingServiceException(String message) {
        super(message);
    }

    public EmbeddingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmbeddingServiceException(Throwable cause) {
        super(cause);
    }
}

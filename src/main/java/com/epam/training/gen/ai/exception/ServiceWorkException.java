package com.epam.training.gen.ai.exception;

public class ServiceWorkException extends RuntimeException {
    public ServiceWorkException() {
        super();
    }

    public ServiceWorkException(String message) {
        super(message);
    }

    public ServiceWorkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceWorkException(Throwable cause) {
        super(cause);
    }
}

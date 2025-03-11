package com.epam.training.gen.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
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

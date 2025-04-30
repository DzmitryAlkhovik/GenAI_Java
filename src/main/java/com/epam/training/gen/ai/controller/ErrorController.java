package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ExceptionDTO;
import com.epam.training.gen.ai.exception.EmbeddingServiceException;
import com.epam.training.gen.ai.exception.ServiceWorkException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler({ServiceWorkException.class, EmbeddingServiceException.class})
    public ResponseEntity<ExceptionDTO> handleIllegalArgumentException(ServiceWorkException ex) {
        return new ResponseEntity<>(new ExceptionDTO(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.axcelerate.homehub.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class NoLastOperationException extends RuntimeException {
    HttpStatus operationStatus;
    private String message;

    public NoLastOperationException(HttpStatus status, String message) {
        super(message);
        this.message = message;
        this.operationStatus = status;
    }
}

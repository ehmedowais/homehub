package com.axcelerate.homehub.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BindException extends RuntimeException {
    private HttpStatus status;
    private String message;

    public BindException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}

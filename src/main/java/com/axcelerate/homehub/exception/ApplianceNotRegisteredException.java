package com.axcelerate.homehub.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter
@Setter
@NoArgsConstructor
public class ApplianceNotRegisteredException extends RuntimeException {
    private HttpStatus status;
    private String message;

    public ApplianceNotRegisteredException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}

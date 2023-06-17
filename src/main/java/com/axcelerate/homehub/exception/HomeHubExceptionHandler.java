package com.axcelerate.homehub.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class HomeHubExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {BindException.class, ApplianceNotRegisteredException.class, ApplianceAlreadyRegisteredException.class,
        NoLastOperationException.class})
    public ResponseEntity<ErrorMessage> homeHubExceptionHandler(RuntimeException ex) {
        var errorMsg = new ErrorMessage(400, new Date(), ex.getMessage(), ex.getLocalizedMessage());
        return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }
}

package com.example.expensetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class IllegalArgumentExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        // Extract field name from error message if possible
        String message = ex.getMessage();
        if (message.contains("Amount")) {
            errors.put("amount", message);
        } else if (message.contains("Date")) {
            errors.put("date", message);
        } else {
            errors.put("error", message);
        }
        
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("errors", errors);
        responseBody.put("message", "Validation failed");
        
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}

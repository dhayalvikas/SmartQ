package com.smartq.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle @Valid annotation errors (field validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(),
                        error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    // FIX #8: Return correct HTTP status codes based on error message
    // Previously everything returned 400 BAD REQUEST
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        String msg = ex.getMessage() != null
                ? ex.getMessage().toLowerCase() : "";

        HttpStatus status = HttpStatus.BAD_REQUEST; // default

        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;           // 404
        } else if (msg.contains("unauthorized")) {
            status = HttpStatus.FORBIDDEN;           // 403
        } else if (msg.contains("already exists")
                || msg.contains("already submitted")
                || msg.contains("already have an active")) {
            status = HttpStatus.CONFLICT;            // 409
        } else if (msg.contains("queue is full")
                || msg.contains("queue is closed")
                || msg.contains("cannot delete")
                || msg.contains("already open")
                || msg.contains("already closed")) {
            status = HttpStatus.CONFLICT;            // 409
        } else if (msg.contains("only a called token")) {
            status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
        }

        return ResponseEntity.status(status).body(error);
    }
}

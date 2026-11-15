package com.demo.department.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.demo.department.config.Constants.CONFLICT_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> notFound(Exception e) {
        return response(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> conflict(Exception e) {
        return response(HttpStatus.CONFLICT, CONFLICT_ERROR, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integrity(DataIntegrityViolationException e) {
        return response(HttpStatus.CONFLICT, CONFLICT_ERROR, "The request violates a data integrity constraint.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException e) {
        return response(HttpStatusCode.valueOf(e.getStatusCode().value()), "Request Failed", e.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> other(Exception e) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
    }

    private ResponseEntity<?> response(HttpStatusCode status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(),
                "status", status.value(), "error", error, "message", message == null ? error : message));
    }
}

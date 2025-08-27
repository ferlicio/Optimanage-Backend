package com.AIT.Optimanage.Controllers.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.DefaultMessageSourceResolvable;

import com.AIT.Optimanage.Exceptions.CustomRuntimeException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(LocalDateTime timestamp, int code, String message,
                                String correlationId, List<String> errors) {}

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Entity not found: {} - correlationId: {}", ex.getMessage(), correlationId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(), correlationId, Collections.emptyList()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Illegal argument: {} - correlationId: {}", ex.getMessage(), correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(), correlationId, Collections.emptyList()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String correlationId = UUID.randomUUID().toString();
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        log.warn("Validation failed: {} - correlationId: {}", errors, correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                        "Validation failed", correlationId, errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Access denied: {} - correlationId: {}", ex.getMessage(), correlationId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                        ex.getMessage(), correlationId, Collections.emptyList()));
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleCustomRuntime(CustomRuntimeException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Runtime exception: {} - correlationId: {}", ex.getMessage(), correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(), correlationId, Collections.emptyList()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Internal server error - correlationId: {}", correlationId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error", correlationId, Collections.emptyList()));
    }
}

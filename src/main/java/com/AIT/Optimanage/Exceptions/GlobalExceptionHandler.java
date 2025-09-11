package com.AIT.Optimanage.Exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE_URI = "https://api.optimanage.com/problems/";

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail,
                                            String type, String correlationId, List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(PROBLEM_BASE_URI + type));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("correlationId", correlationId);
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Entity not found: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Entity not found",
                ex.getMessage(), "entity-not-found", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Illegal argument: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad request",
                ex.getMessage(), "illegal-argument", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String correlationId = MDC.get("correlationId");
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {} - correlationId: {}", errors, correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed",
                "Validation failed", "validation-error", correlationId, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        String correlationId = MDC.get("correlationId");
        List<String> errors = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        log.warn("Validation failed: {} - correlationId: {}", errors, correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed",
                "Validation failed", "validation-error", correlationId, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Access denied: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.FORBIDDEN, "Access denied",
                ex.getMessage(), "access-denied", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(LockedException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Account locked: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.LOCKED, "Account locked",
                ex.getMessage(), "account-locked", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.LOCKED).body(problem);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("User not found: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "User not found",
                ex.getMessage(), "user-not-found", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler({InvalidTwoFactorCodeException.class, InvalidResetCodeException.class})
    public ResponseEntity<ProblemDetail> handleInvalidCode(CustomRuntimeException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Invalid code: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid code",
                ex.getMessage(), "invalid-code", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Refresh token not found: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Refresh token not found",
                ex.getMessage(), "refresh-token-not-found", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ProblemDetail> handleRefreshTokenInvalid(RefreshTokenInvalidException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Refresh token invalid: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNAUTHORIZED, "Refresh token invalid",
                ex.getMessage(), "refresh-token-invalid", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleCustomRuntime(CustomRuntimeException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Runtime exception: {} - correlationId: {}", ex.getMessage(), correlationId);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad request",
                ex.getMessage(), "runtime-exception", correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex) {
        String correlationId = MDC.get("correlationId");
        log.error("Internal server error - correlationId: {}", correlationId, ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", "Internal server error", "internal-server-error",
                correlationId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}

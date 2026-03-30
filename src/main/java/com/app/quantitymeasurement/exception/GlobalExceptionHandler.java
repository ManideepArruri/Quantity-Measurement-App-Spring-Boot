package com.app.quantitymeasurement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 *
 * Three handler methods:
 *  1. handleValidationException   — @Valid failures (HTTP 400)
 *  2. handleQuantityException     — QuantityMeasurementException (HTTP 400)
 *  3. handleGlobalException       — everything else (HTTP 500)
 *
 * Every response follows the same ErrorResponse structure so clients
 * always know what shape to expect on failure.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            Logger.getLogger(GlobalExceptionHandler.class.getName());

    // ── Shared error response shape ──────────────────────────────────────────

    static class ErrorResponse {
        public LocalDateTime timestamp;
        public int           status;
        public String        error;
        public String        message;
        public String        path;
    }

    // ── 1. Bean Validation failures (@Valid) ─────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        logger.warning("Validation failed: " + ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse response  = new ErrorResponse();
        response.timestamp      = LocalDateTime.now();
        response.status         = HttpStatus.BAD_REQUEST.value();
        response.error          = "Quantity Measurement Error";
        response.message        = String.join("; ", errors);
        response.path           = "quantityInputDTO";

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ── 2. Domain exceptions ─────────────────────────────────────────────────

    @ExceptionHandler(QuantityMeasurementException.class)
    public ResponseEntity<ErrorResponse> handleQuantityException(
            QuantityMeasurementException ex,
            HttpServletRequest request) {

        logger.warning("QuantityMeasurementException: " + ex.getMessage()
                + " for request path: " + request.getRequestURI());

        ErrorResponse response  = new ErrorResponse();
        response.timestamp      = LocalDateTime.now();
        response.status         = HttpStatus.BAD_REQUEST.value();
        response.error          = "Quantity Measurement Error";
        response.message        = ex.getMessage();
        response.path           = request.getRequestURI();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ── 3. Catch-all ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        logger.severe("Unhandled exception: " + ex.getMessage()
                + " for request path: " + request.getRequestURI());

        ErrorResponse response  = new ErrorResponse();
        response.timestamp      = LocalDateTime.now();
        response.status         = HttpStatus.INTERNAL_SERVER_ERROR.value();
        response.error          = "Internal Server Error";
        response.message        = ex.getMessage();
        response.path           = request.getRequestURI();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
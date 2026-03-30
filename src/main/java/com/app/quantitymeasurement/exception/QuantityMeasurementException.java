package com.app.quantitymeasurement.exception;

/**
 * Custom exception for all domain-level errors in the quantity
 * measurement application — e.g. incompatible measurement types,
 * division by zero, unsupported unit conversions.
 *
 * Caught by GlobalExceptionHandler and returned as HTTP 400.
 */
public class QuantityMeasurementException extends RuntimeException {

    public QuantityMeasurementException(String message) {
        super(message);
    }

    public QuantityMeasurementException(String message, Throwable cause) {
        super(message, cause);
    }
}
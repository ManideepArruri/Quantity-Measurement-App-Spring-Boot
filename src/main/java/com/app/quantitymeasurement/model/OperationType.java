package com.app.quantitymeasurement.model;

/**
 * Enum representing all supported operation types.
 * Used in QuantityMeasurementDTO and QuantityMeasurementEntity
 * to tag every persisted operation with its type.
 */
public enum OperationType {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    COMPARE,
    CONVERT;

    /** Returns the lowercase display name, e.g. "compare". */
    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
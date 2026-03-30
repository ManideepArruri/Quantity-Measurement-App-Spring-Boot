
package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;

import java.util.List;

/**
 * Service interface declaring all quantity measurement operations.
 * The controller depends only on this interface — never on the implementation.
 * This makes it easy to swap or mock the implementation in tests.
 */
public interface IQuantityMeasurementService {

    /** Compare two quantities. resultString = "true" if equal, "false" otherwise. */
    QuantityMeasurementDTO compare(QuantityDTO thisQuantityDTO,
                                   QuantityDTO thatQuantityDTO);

    /** Convert thisQuantity to the unit declared in thatQuantityDTO. */
    QuantityMeasurementDTO convert(QuantityDTO thisQuantityDTO,
                                   QuantityDTO thatQuantityDTO);

    /** Add two quantities. Result unit = thisQuantityDTO's unit. */
    QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO,
                               QuantityDTO thatQuantityDTO);

    /** Add two quantities, expressing the result in targetUnitDTO's unit. */
    QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO,
                               QuantityDTO thatQuantityDTO,
                               QuantityDTO targetUnitDTO);

    /** Subtract thatQuantity from thisQuantity. Result unit = thisQuantityDTO's unit. */
    QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO,
                                    QuantityDTO thatQuantityDTO);

    /** Subtract, expressing the result in targetUnitDTO's unit. */
    QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO,
                                    QuantityDTO thatQuantityDTO,
                                    QuantityDTO targetUnitDTO);

    /** Divide thisQuantity by thatQuantity. */
    QuantityMeasurementDTO divide(QuantityDTO thisQuantityDTO,
                                  QuantityDTO thatQuantityDTO);

    // ── History / analytics ──────────────────────────────────────────────────

    /** All stored measurements for a given operation type (e.g. "COMPARE"). */
    List<QuantityMeasurementDTO> getOperationHistory(String operation);

    /** All stored measurements where the first operand had the given type. */
    List<QuantityMeasurementDTO> getMeasurementsByType(String type);

    /** Count of successful (non-error) operations for a given operation type. */
    long getOperationCount(String operation);

    /** All measurements that resulted in an error. */
    List<QuantityMeasurementDTO> getErrorHistory();
}
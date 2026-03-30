package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.model.OperationType;
import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service implementation handling all quantity measurement business logic.
 *
 * Key design decisions (from UC17):
 *  - No @Transactional at class level — we intentionally save error results
 *    to the DB even when an operation fails, so a rolled-back transaction
 *    would lose that audit record.
 *  - Field injection via @Autowired for simplicity; constructor injection
 *    is preferred for testability if you refactor later.
 *  - Every operation saves its result (success or error) to the repository
 *    so history/count endpoints always have data to return.
 */
@Service
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private static final Logger logger =
            Logger.getLogger(QuantityMeasurementServiceImpl.class.getName());

    @Autowired
    private QuantityMeasurementRepository repository;

    // ── COMPARE ──────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO compare(QuantityDTO thisQty, QuantityDTO thatQty) {
        logger.info("Comparing: " + thisQty + " vs " + thatQty);
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            validateSameMeasurementType(thisQty, thatQty, "compare");
            double thisInBase = toBaseUnit(thisQty);
            double thatInBase = toBaseUnit(thatQty);
            boolean equal     = Double.compare(thisInBase, thatInBase) == 0;

            populateOperands(result, thisQty, thatQty);
            result.setOperation(OperationType.COMPARE.name());
            result.setResultString(String.valueOf(equal));
            result.setError(false);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.COMPARE, e.getMessage());
        }
        return saveAndReturn(result);
    }

    // ── CONVERT ──────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO convert(QuantityDTO thisQty, QuantityDTO thatQty) {
        logger.info("Converting: " + thisQty + " → " + thatQty.getUnit());
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            validateSameMeasurementType(thisQty, thatQty, "convert");
            double converted = convertValue(thisQty, thatQty.getUnit());

            populateOperands(result, thisQty, thatQty);
            result.setOperation(OperationType.CONVERT.name());
            result.setResultValue(converted);
            result.setResultUnit(thatQty.getUnit());
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setError(false);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.CONVERT, e.getMessage());
        }
        return saveAndReturn(result);
    }

    // ── ADD (same unit) ──────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty) {
        return performArithmetic(thisQty, thatQty, null, OperationType.ADD);
    }

    // ── ADD (with target unit) ───────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty,
                                      QuantityDTO targetUnit) {
        return performArithmetic(thisQty, thatQty, targetUnit, OperationType.ADD);
    }

    // ── SUBTRACT (same unit) ─────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty) {
        return performArithmetic(thisQty, thatQty, null, OperationType.SUBTRACT);
    }

    // ── SUBTRACT (with target unit) ──────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty,
                                            QuantityDTO targetUnit) {
        return performArithmetic(thisQty, thatQty, targetUnit, OperationType.SUBTRACT);
    }

    // ── DIVIDE ───────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO divide(QuantityDTO thisQty, QuantityDTO thatQty) {
        logger.info("Dividing: " + thisQty + " / " + thatQty);
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            validateSameMeasurementType(thisQty, thatQty, "divide");
            double thatInBase = toBaseUnit(thatQty);
            if (Double.compare(thatInBase, 0.0) == 0) {
                throw new ArithmeticException("Divide by zero");
            }
            double thisInBase  = toBaseUnit(thisQty);
            double quotient    = thisInBase / thatInBase;

            populateOperands(result, thisQty, thatQty);
            result.setOperation(OperationType.DIVIDE.name());
            result.setResultValue(quotient);
            result.setResultUnit(thisQty.getUnit());
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setError(false);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.DIVIDE, e.getMessage());
        }
        return saveAndReturn(result);
    }

    // ── HISTORY / ANALYTICS ──────────────────────────────────────────────────

    @Override
    public List<QuantityMeasurementDTO> getOperationHistory(String operation) {
        return QuantityMeasurementDTO.fromEntityList(
                repository.findByOperation(operation.toUpperCase()));
    }

    @Override
    public List<QuantityMeasurementDTO> getMeasurementsByType(String type) {
        return QuantityMeasurementDTO.fromEntityList(
                repository.findByThisMeasurementType(type));
    }

    @Override
    public long getOperationCount(String operation) {
        return repository.countByOperationAndIsErrorFalse(operation.toUpperCase());
    }

    @Override
    public List<QuantityMeasurementDTO> getErrorHistory() {
        return QuantityMeasurementDTO.fromEntityList(
                repository.findByIsErrorTrue());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Core arithmetic handler shared by add/subtract with or without target unit. */
    private QuantityMeasurementDTO performArithmetic(
            QuantityDTO thisQty, QuantityDTO thatQty,
            QuantityDTO targetUnit, OperationType opType) {

        logger.info(opType + ": " + thisQty + ", " + thatQty);
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            validateSameMeasurementType(thisQty, thatQty,
                    opType.name().toLowerCase());

            double thisInBase = toBaseUnit(thisQty);
            double thatInBase = toBaseUnit(thatQty);
            double rawResult  = opType == OperationType.ADD
                    ? thisInBase + thatInBase
                    : thisInBase - thatInBase;

            String outputUnit = targetUnit != null
                    ? targetUnit.getUnit()
                    : thisQty.getUnit();

            // Convert raw base-unit result back to desired output unit
            double finalResult = fromBaseUnit(rawResult, outputUnit,
                    thisQty.getMeasurementType());

            populateOperands(result, thisQty, thatQty);
            result.setOperation(opType.name());
            result.setResultValue(finalResult);
            result.setResultUnit(outputUnit);
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setError(false);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, opType, e.getMessage());
        }
        return saveAndReturn(result);
    }

    /** Validates both quantities share the same measurement type. */
    private void validateSameMeasurementType(QuantityDTO a, QuantityDTO b,
                                              String operation) {
        if (!a.getMeasurementType().equals(b.getMeasurementType())) {
            throw new QuantityMeasurementException(
                    operation + " Error: Cannot perform arithmetic between different"
                    + " measurement categories: "
                    + a.getMeasurementType() + " and " + b.getMeasurementType());
        }
    }

    /**
     * Converts a quantity to its canonical base unit value.
     * Base units: INCHES (length), MILLILITER (volume), GRAM (weight), CELSIUS (temp).
     * NOTE: Replace these stubs with your actual unit-conversion logic from UC1-UC16.
     */
    private double toBaseUnit(QuantityDTO qty) {
        double v = qty.getValue();
        switch (qty.getMeasurementType()) {
            case "LengthUnit":
                switch (qty.getUnit().toUpperCase()) {
                    case "FEET":       return v * 12.0;
                    case "YARDS":      return v * 36.0;
                    case "CENTIMETERS":return v / 2.54;
                    case "METERS":     return v / 2.54 * 100;
                    case "KILOMETERS": return v / 2.54 * 100000;
                    case "MILES":      return v * 63360;
                    default:           return v; // INCHES = base
                }
            case "VolumeUnit":
                switch (qty.getUnit().toUpperCase()) {
                    case "LITRE":      return v * 1000.0;
                    case "GALLON":     return v * 3785.41;
                    case "CUBIC_METER":return v * 1_000_000.0;
                    default:           return v; // MILLILITER = base
                }
            case "WeightUnit":
                switch (qty.getUnit().toUpperCase()) {
                    case "KILOGRAM":   return v * 1000.0;
                    case "POUND":      return v * 453.592;
                    case "TONNE":      return v * 1_000_000.0;
                    case "MILLIGRAM":  return v / 1000.0;
                    default:           return v; // GRAM = base
                }
            case "TemperatureUnit":
                switch (qty.getUnit().toUpperCase()) {
                    case "FAHRENHEIT": return (v - 32) * 5.0 / 9.0;
                    case "KELVIN":     return v - 273.15;
                    default:           return v; // CELSIUS = base
                }
            default:
                throw new QuantityMeasurementException(
                        "Unknown measurement type: " + qty.getMeasurementType());
        }
    }

    /** Converts a base-unit value back into the specified output unit. */
    private double fromBaseUnit(double baseValue, String targetUnit,
                                 String measurementType) {
        switch (measurementType) {
            case "LengthUnit":
                switch (targetUnit.toUpperCase()) {
                    case "FEET":        return baseValue / 12.0;
                    case "YARDS":       return baseValue / 36.0;
                    case "CENTIMETERS": return baseValue * 2.54;
                    case "METERS":      return baseValue * 2.54 / 100;
                    case "KILOMETERS":  return baseValue * 2.54 / 100000;
                    case "MILES":       return baseValue / 63360;
                    default:            return baseValue; // INCHES
                }
            case "VolumeUnit":
                switch (targetUnit.toUpperCase()) {
                    case "LITRE":       return baseValue / 1000.0;
                    case "GALLON":      return baseValue / 3785.41;
                    default:            return baseValue; // MILLILITER
                }
            case "WeightUnit":
                switch (targetUnit.toUpperCase()) {
                    case "KILOGRAM":    return baseValue / 1000.0;
                    case "POUND":       return baseValue / 453.592;
                    case "TONNE":       return baseValue / 1_000_000.0;
                    case "MILLIGRAM":   return baseValue * 1000.0;
                    default:            return baseValue; // GRAM
                }
            case "TemperatureUnit":
                switch (targetUnit.toUpperCase()) {
                    case "FAHRENHEIT":  return baseValue * 9.0 / 5.0 + 32;
                    case "KELVIN":      return baseValue + 273.15;
                    default:            return baseValue; // CELSIUS
                }
            default:
                throw new QuantityMeasurementException(
                        "Unknown measurement type: " + measurementType);
        }
    }

    /** Converts thisQty to a specific target unit (used by convert endpoint). */
    private double convertValue(QuantityDTO source, String targetUnit) {
        double base = toBaseUnit(source);
        return fromBaseUnit(base, targetUnit, source.getMeasurementType());
    }

    /** Populates the two operand fields on a result DTO. */
    private void populateOperands(QuantityMeasurementDTO dto,
                                   QuantityDTO thisQty, QuantityDTO thatQty) {
        dto.setThisValue(thisQty.getValue());
        dto.setThisUnit(thisQty.getUnit());
        dto.setThisMeasurementType(thisQty.getMeasurementType());
        dto.setThatValue(thatQty.getValue());
        dto.setThatUnit(thatQty.getUnit());
        dto.setThatMeasurementType(thatQty.getMeasurementType());
    }

    /** Populates the error fields on a result DTO. */
    private void populateError(QuantityMeasurementDTO dto,
                                QuantityDTO thisQty, QuantityDTO thatQty,
                                OperationType opType, String message) {
        logger.warning(opType + " error: " + message);
        if (thisQty != null) populateOperands(dto, thisQty, thatQty);
        dto.setOperation(opType.name());
        dto.setError(true);
        dto.setErrorMessage(message);
    }

    /** Saves the result entity to the DB and returns the DTO. */
    private QuantityMeasurementDTO saveAndReturn(QuantityMeasurementDTO dto) {
        try {
            QuantityMeasurementEntity saved = repository.save(dto.toEntity());
            return QuantityMeasurementDTO.fromEntity(saved);
        } catch (Exception e) {
            logger.severe("Failed to save result: " + e.getMessage());
            return dto;
        }
    }
}

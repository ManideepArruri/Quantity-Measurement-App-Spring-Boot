
package com.app.quantitymeasurement.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.logging.Logger;

/**
 * DTO representing a single measurable quantity.
 * Used as a building block inside QuantityInputDTO.
 *
 * Validation rules enforced before the request reaches the service:
 *  - value must not be null
 *  - unit must not be null
 *  - measurementType must be one of the four supported types
 *  - isValidUnit() cross-checks that the unit belongs to the declared type
 */
@Data
@NoArgsConstructor
public class QuantityDTO {

    private static final Logger logger = Logger.getLogger(QuantityDTO.class.getName());

    // ── Measurement type constraint ──────────────────────────────────────────
    private static final String TYPE_REGEX =
            "LengthUnit|VolumeUnit|WeightUnit|TemperatureUnit";

    // ── Valid unit sets (must match the enums in your unit package) ──────────
    private static final java.util.Set<String> LENGTH_UNITS = java.util.Set.of(
            "FEET", "INCHES", "YARDS", "CENTIMETERS", "METERS", "KILOMETERS", "MILES"
    );
    private static final java.util.Set<String> VOLUME_UNITS = java.util.Set.of(
            "LITRE", "MILLILITER", "GALLON", "CUBIC_METER", "CUBIC_CENTIMETER"
    );
    private static final java.util.Set<String> WEIGHT_UNITS = java.util.Set.of(
            "GRAM", "KILOGRAM", "MILLIGRAM", "POUND", "TONNE"
    );
    private static final java.util.Set<String> TEMPERATURE_UNITS = java.util.Set.of(
            "CELSIUS", "FAHRENHEIT", "KELVIN"
    );

    // ── Fields ───────────────────────────────────────────────────────────────

    @NotNull(message = "Value cannot be null")
    private Double value;

    @NotNull(message = "Unit cannot be null")
    private String unit;

    @NotNull(message = "Measurement type cannot be null")
    @Pattern(
        regexp = TYPE_REGEX,
        message = "Measurement type must be one of: LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit"
    )
    private String measurementType;
    
    

    // ── Convenience constructor ──────────────────────────────────────────────

    public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static String getTypeRegex() {
		return TYPE_REGEX;
	}

	public static java.util.Set<String> getLengthUnits() {
		return LENGTH_UNITS;
	}

	public static java.util.Set<String> getVolumeUnits() {
		return VOLUME_UNITS;
	}

	public static java.util.Set<String> getWeightUnits() {
		return WEIGHT_UNITS;
	}

	public static java.util.Set<String> getTemperatureUnits() {
		return TEMPERATURE_UNITS;
	}

	public QuantityDTO(double value, String unit, String measurementType) {
        this.value           = value;
        this.unit            = unit;
        this.measurementType = measurementType;
    }

    // ── Cross-field validation ───────────────────────────────────────────────

    /**
     * Checks that the supplied unit is valid for the declared measurementType.
     * Called automatically by Bean Validation via @AssertTrue.
     */
    @AssertTrue(message = "Unit must be valid for the specified measurement type")
    public boolean isValidUnit() {
        if (unit == null || measurementType == null) return true; // let @NotNull handle nulls
        logger.info("Validating unit: " + unit + " for measurement type: " + measurementType);
        try {
            switch (measurementType) {
                case "LengthUnit":      return LENGTH_UNITS.contains(unit.toUpperCase());
                case "VolumeUnit":      return VOLUME_UNITS.contains(unit.toUpperCase());
                case "WeightUnit":      return WEIGHT_UNITS.contains(unit.toUpperCase());
                case "TemperatureUnit": return TEMPERATURE_UNITS.contains(unit.toUpperCase());
                default:                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
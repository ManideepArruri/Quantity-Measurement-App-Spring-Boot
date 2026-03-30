
package com.app.quantitymeasurement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representing the result of a quantity measurement operation.
 *
 * Sent back to the client as JSON. Never persisted directly — use the
 * static factory methods to convert to/from QuantityMeasurementEntity.
 *
 * Static factory methods:
 *   fromEntity(entity)       — entity  → DTO  (used when returning results)
 *   toEntity()               — DTO     → entity (used before saving)
 *   fromEntityList(entities) — List<entity> → List<DTO>
 *   toEntityList(dtos)       — List<DTO>    → List<entity>
 */
@Data
@NoArgsConstructor
public class QuantityMeasurementDTO {

    // ── First operand ────────────────────────────────────────────────────────
    private double thisValue;
    private String thisUnit;
    private String thisMeasurementType;

    // ── Second operand ───────────────────────────────────────────────────────
    private double thatValue;
    private String thatUnit;
    private String thatMeasurementType;

    // ── Operation ────────────────────────────────────────────────────────────
    private String operation;

    // ── Result ───────────────────────────────────────────────────────────────
    private String resultString;        // "true"/"false" for comparisons
    private double resultValue;         // numeric result for arithmetic/conversion
    private String resultUnit;
    private String resultMeasurementType;

    // ── Error state ──────────────────────────────────────────────────────────
    /**
     * Jackson strips the "is" prefix from boolean getters by default,
     * turning isError() → "error" in JSON. @JsonProperty fixes the key name.
     */
    @JsonProperty("error")
    private boolean error;

    private String errorMessage;
    
    
    

    // ── Static factory: Entity → DTO ─────────────────────────────────────────

    public double getThisValue() {
		return thisValue;
	}

	public void setThisValue(double thisValue) {
		this.thisValue = thisValue;
	}

	public String getThisUnit() {
		return thisUnit;
	}

	public void setThisUnit(String thisUnit) {
		this.thisUnit = thisUnit;
	}

	public String getThisMeasurementType() {
		return thisMeasurementType;
	}

	public void setThisMeasurementType(String thisMeasurementType) {
		this.thisMeasurementType = thisMeasurementType;
	}

	public double getThatValue() {
		return thatValue;
	}

	public void setThatValue(double thatValue) {
		this.thatValue = thatValue;
	}

	public String getThatUnit() {
		return thatUnit;
	}

	public void setThatUnit(String thatUnit) {
		this.thatUnit = thatUnit;
	}

	public String getThatMeasurementType() {
		return thatMeasurementType;
	}

	public void setThatMeasurementType(String thatMeasurementType) {
		this.thatMeasurementType = thatMeasurementType;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}

	public double getResultValue() {
		return resultValue;
	}

	public void setResultValue(double resultValue) {
		this.resultValue = resultValue;
	}

	public String getResultUnit() {
		return resultUnit;
	}

	public void setResultUnit(String resultUnit) {
		this.resultUnit = resultUnit;
	}

	public String getResultMeasurementType() {
		return resultMeasurementType;
	}

	public void setResultMeasurementType(String resultMeasurementType) {
		this.resultMeasurementType = resultMeasurementType;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
     * Converts a persisted QuantityMeasurementEntity to this DTO.
     * Called by the service after saving, so the response always reflects
     * what was actually stored.
     */
    public static QuantityMeasurementDTO fromEntity(QuantityMeasurementEntity entity) {
        if (entity == null) return null;
        QuantityMeasurementDTO dto = new QuantityMeasurementDTO();
        dto.thisValue              = entity.getThisValue();
        dto.thisUnit               = entity.getThisUnit();
        dto.thisMeasurementType    = entity.getThisMeasurementType();
        dto.thatValue              = entity.getThatValue();
        dto.thatUnit               = entity.getThatUnit();
        dto.thatMeasurementType    = entity.getThatMeasurementType();
        dto.operation              = entity.getOperation();
        dto.resultString           = entity.getResultString();
        dto.resultValue            = entity.getResultValue();
        dto.resultUnit             = entity.getResultUnit();
        dto.resultMeasurementType  = entity.getResultMeasurementType();
        dto.error                  = entity.isError();
        dto.errorMessage           = entity.getErrorMessage();
        return dto;
    }

    // ── Instance method: DTO → Entity ────────────────────────────────────────

    /**
     * Converts this DTO to a new QuantityMeasurementEntity ready for saving.
     * Timestamps are set automatically by @PrePersist on the entity.
     */
    public QuantityMeasurementEntity toEntity() {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
        entity.setThisValue(this.thisValue);
        entity.setThisUnit(this.thisUnit);
        entity.setThisMeasurementType(this.thisMeasurementType);
        entity.setThatValue(this.thatValue);
        entity.setThatUnit(this.thatUnit);
        entity.setThatMeasurementType(this.thatMeasurementType);
        entity.setOperation(this.operation);
        entity.setResultString(this.resultString);
        entity.setResultValue(this.resultValue);
        entity.setResultUnit(this.resultUnit);
        entity.setResultMeasurementType(this.resultMeasurementType);
        entity.setError(this.error);
        entity.setErrorMessage(this.errorMessage);
        return entity;
    }

    // ── Collection converters ────────────────────────────────────────────────

    /**
     * Maps a list of entities to a list of DTOs using Java Streams.
     * Used by service methods that return history lists.
     */
    public static List<QuantityMeasurementDTO> fromEntityList(
            List<QuantityMeasurementEntity> entities) {
        return entities.stream()
                .map(QuantityMeasurementDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of DTOs to a list of entities.
     * Useful when bulk-saving results.
     */
    public static List<QuantityMeasurementEntity> toEntityList(
            List<QuantityMeasurementDTO> dtos) {
        return dtos.stream()
                .map(QuantityMeasurementDTO::toEntity)
                .collect(Collectors.toList());
    }
}
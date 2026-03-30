
package com.app.quantitymeasurement.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that wraps the full request body for any quantity operation endpoint.
 *
 * Most endpoints (compare, convert, add, subtract, divide) only need
 * thisQuantityDTO + thatQuantityDTO.
 *
 * The add-with-target and subtract-with-target endpoints also supply a
 * targetQuantityDTO that declares which unit the result should be in.
 *
 * @Valid on each nested DTO triggers cascaded Bean Validation — if any
 * inner QuantityDTO fails its constraints the whole request is rejected
 * with HTTP 400 before the service is called.
 */
@Data
@NoArgsConstructor
public class QuantityInputDTO {

    @Valid
    @NotNull(message = "First quantity cannot be null")
    private QuantityDTO thisQuantityDTO;

    @Valid
    @NotNull(message = "Second quantity cannot be null")
    private QuantityDTO thatQuantityDTO;

    /**
     * Optional — only required for endpoints that accept a target unit
     * (add-with-target-unit, subtract-with-target-unit).
     */
    @Valid
    private QuantityDTO targetQuantityDTO;

	public QuantityDTO getThisQuantityDTO() {
		return thisQuantityDTO;
	}

	public void setThisQuantityDTO(QuantityDTO thisQuantityDTO) {
		this.thisQuantityDTO = thisQuantityDTO;
	}

	public QuantityDTO getThatQuantityDTO() {
		return thatQuantityDTO;
	}

	public void setThatQuantityDTO(QuantityDTO thatQuantityDTO) {
		this.thatQuantityDTO = thatQuantityDTO;
	}

	public QuantityDTO getTargetQuantityDTO() {
		return targetQuantityDTO;
	}

	public void setTargetQuantityDTO(QuantityDTO targetQuantityDTO) {
		this.targetQuantityDTO = targetQuantityDTO;
	}
	
	
    
    
}
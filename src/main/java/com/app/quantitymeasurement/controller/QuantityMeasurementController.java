
package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * REST controller exposing all quantity measurement API endpoints.
 *
 * Base URL : /api/v1/quantities
 *
 * POST endpoints (mutation — they run an operation and save the result):
 *   /compare                  — compare two quantities
 *   /convert                  — convert to a target unit
 *   /add                      — add two quantities
 *   /add-with-target-unit     — add and express result in a specific unit
 *   /subtract                 — subtract two quantities
 *   /subtract-with-target-unit— subtract and express result in a specific unit
 *   /divide                   — divide two quantities
 *
 * GET endpoints (read-only — query persisted history):
 *   /history/operation/{op}   — all operations of type op (COMPARE, ADD …)
 *   /history/type/{type}      — all operations for a measurement type
 *   /count/{operation}        — count of successful operations
 *   /history/errored          — all operations that resulted in errors
 */
@RestController
@RequestMapping("/api/v1/quantities")
@Tag(name = "Quantity Measurements",
     description = "REST API for quantity measurement operations")
public class QuantityMeasurementController {

    private static final Logger logger =
            Logger.getLogger(QuantityMeasurementController.class.getName());

    @Autowired
    private IQuantityMeasurementService service;

    // ════════════════════════════════════════════════════════════════════════
    // POST — operations
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/quantities/compare
     *
     * Compares two quantities of the same type.
     * Both are converted to their base unit before comparing.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO)
     * Response     : QuantityMeasurementDTO — resultString = "true" | "false"
     */
    @PostMapping("/compare")
    @Operation(summary = "Compare two quantities",
               description = "Returns true if the two quantities are equal after unit conversion")
    public ResponseEntity<QuantityMeasurementDTO> performComparison(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /compare called");
        QuantityMeasurementDTO result = service.compare(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/convert
     *
     * Converts thisQuantityDTO to the unit declared in thatQuantityDTO.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO)
     *                thatQuantityDTO.unit = the target unit
     * Response     : QuantityMeasurementDTO — resultValue in thatQuantityDTO.unit
     */
    @PostMapping("/convert")
    @Operation(summary = "Convert quantity to target unit",
               description = "Converts the first quantity to the unit specified by the second quantity")
    public ResponseEntity<QuantityMeasurementDTO> performConversion(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /convert called");
        QuantityMeasurementDTO result = service.convert(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/add
     *
     * Adds two quantities of the same type.
     * Result is expressed in thisQuantityDTO's unit.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO)
     * Response     : QuantityMeasurementDTO — resultValue in thisQuantityDTO.unit
     */
    @PostMapping("/add")
    @Operation(summary = "Add two quantities",
               description = "Adds two same-type quantities; result is in the first quantity's unit")
    public ResponseEntity<QuantityMeasurementDTO> performAddition(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /add called");
        QuantityMeasurementDTO result = service.add(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/add-with-target-unit
     *
     * Adds two quantities and expresses the result in a specific target unit.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO + targetQuantityDTO)
     *                targetQuantityDTO.unit = the desired result unit
     * Response     : QuantityMeasurementDTO — resultValue in targetQuantityDTO.unit
     */
    @PostMapping("/add-with-target-unit")
    @Operation(summary = "Add two quantities with target unit",
               description = "Adds two quantities and converts the result into the specified target unit")
    public ResponseEntity<QuantityMeasurementDTO> performAdditionWithTargetUnit(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /add-with-target-unit called");
        QuantityMeasurementDTO result = service.add(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO(),
                input.getTargetQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/subtract
     *
     * Subtracts thatQuantityDTO from thisQuantityDTO.
     * Result is expressed in thisQuantityDTO's unit.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO)
     * Response     : QuantityMeasurementDTO — resultValue in thisQuantityDTO.unit
     */
    @PostMapping("/subtract")
    @Operation(summary = "Subtract two quantities",
               description = "Subtracts the second quantity from the first; result is in the first quantity's unit")
    public ResponseEntity<QuantityMeasurementDTO> performSubtraction(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /subtract called");
        QuantityMeasurementDTO result = service.subtract(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/subtract-with-target-unit
     *
     * Subtracts thatQuantityDTO from thisQuantityDTO and converts the result
     * to the specified target unit.
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO + targetQuantityDTO)
     * Response     : QuantityMeasurementDTO — resultValue in targetQuantityDTO.unit
     */
    @PostMapping("/subtract-with-target-unit")
    @Operation(summary = "Subtract two quantities with target unit",
               description = "Subtracts the second from the first and converts the result to the target unit")
    public ResponseEntity<QuantityMeasurementDTO> performSubtractionWithTargetUnit(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /subtract-with-target-unit called");
        QuantityMeasurementDTO result = service.subtract(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO(),
                input.getTargetQuantityDTO());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/quantities/divide
     *
     * Divides thisQuantityDTO by thatQuantityDTO.
     * Both are converted to base unit before dividing.
     * Returns HTTP 500 if thatQuantity resolves to zero (divide by zero).
     *
     * Request body : QuantityInputDTO (thisQuantityDTO + thatQuantityDTO)
     * Response     : QuantityMeasurementDTO — resultValue (dimensionless ratio)
     */
    @PostMapping("/divide")
    @Operation(summary = "Divide two quantities",
               description = "Divides the first quantity by the second; throws if divisor is zero")
    public ResponseEntity<QuantityMeasurementDTO> performDivision(
            @Valid @RequestBody QuantityInputDTO input) {

        logger.info("POST /divide called");
        QuantityMeasurementDTO result = service.divide(
                input.getThisQuantityDTO(),
                input.getThatQuantityDTO());
        return ResponseEntity.ok(result);
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET — history / analytics
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/quantities/history/operation/{operation}
     *
     * Returns all stored measurements for the given operation type.
     * Valid values: ADD, SUBTRACT, MULTIPLY, DIVIDE, COMPARE, CONVERT
     *
     * Path variable : operation — e.g. COMPARE
     * Response      : List<QuantityMeasurementDTO>
     */
    @GetMapping("/history/operation/{operation}")
    @Operation(summary = "Get operation history",
               description = "Returns all measurements for a given operation type. Valid: ADD, SUBTRACT, DIVIDE, COMPARE, CONVERT")
    public ResponseEntity<List<QuantityMeasurementDTO>> getOperationHistory(
            @PathVariable String operation) {

        logger.info("GET /history/operation/" + operation);
        return ResponseEntity.ok(service.getOperationHistory(operation));
    }

    /**
     * GET /api/v1/quantities/history/type/{type}
     *
     * Returns all stored measurements where the first operand had the given
     * measurement type.
     * Valid values: LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit
     *
     * Path variable : type — e.g. LengthUnit
     * Response      : List<QuantityMeasurementDTO>
     */
    @GetMapping("/history/type/{type}")
    @Operation(summary = "Get operation history by measurement type",
               description = "Returns all measurements where the first operand matched the given type")
    public ResponseEntity<List<QuantityMeasurementDTO>> getOperationHistoryByType(
            @PathVariable String type) {

        logger.info("GET /history/type/" + type);
        return ResponseEntity.ok(service.getMeasurementsByType(type));
    }

    /**
     * GET /api/v1/quantities/count/{operation}
     *
     * Returns the count of successful (non-error) operations of the given type.
     *
     * Path variable : operation — e.g. COMPARE
     * Response      : Long (plain number)
     */
    @GetMapping("/count/{operation}")
    @Operation(summary = "Get operation count",
               description = "Returns the count of successful (non-error) operations for the given type")
    public ResponseEntity<Long> getOperationCount(
            @PathVariable String operation) {

        logger.info("GET /count/" + operation);
        return ResponseEntity.ok(service.getOperationCount(operation));
    }

    /**
     * GET /api/v1/quantities/history/errored
     *
     * Returns all stored measurements that resulted in an error.
     * Useful for debugging and audit.
     *
     * Response : List<QuantityMeasurementDTO> — all entries where error = true
     */
    @GetMapping("/history/errored")
    @Operation(summary = "Get errored operations history",
               description = "Returns all measurements that resulted in an error")
    public ResponseEntity<List<QuantityMeasurementDTO>> getErroredOperations() {

        logger.info("GET /history/errored");
        return ResponseEntity.ok(service.getErrorHistory());
    }
}
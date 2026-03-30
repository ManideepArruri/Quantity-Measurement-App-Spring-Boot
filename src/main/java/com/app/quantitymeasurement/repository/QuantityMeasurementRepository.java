package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for QuantityMeasurementEntity.
 *
 * Extending JpaRepository gives us save(), findById(), findAll(),
 * deleteById(), count(), and pagination — all for free, no SQL needed.
 *
 * Method-name-derived queries (Spring generates the SQL automatically):
 *   findByOperation                → WHERE operation = ?
 *   findByThisMeasurementType      → WHERE this_measurement_type = ?
 *   findByCreatedAtAfter           → WHERE created_at > ?
 *   countByOperationAndIsErrorFalse→ COUNT WHERE operation = ? AND is_error = false
 *   findByIsErrorTrue              → WHERE is_error = true
 *
 * @Query annotation for custom JPQL when method names get too long:
 *   findSuccessfulOperations       → custom JPQL query
 */
@Repository
public interface QuantityMeasurementRepository
        extends JpaRepository<QuantityMeasurementEntity, Long> {

    // ── Method-name derived queries ──────────────────────────────────────────

    /** All measurements for a given operation type (e.g. "COMPARE"). */
    List<QuantityMeasurementEntity> findByOperation(String operation);

    /** All measurements where the first operand has a given measurement type. */
    List<QuantityMeasurementEntity> findByThisMeasurementType(String measurementType);

    /** All measurements created after a given date/time. */
    List<QuantityMeasurementEntity> findByCreatedAtAfter(LocalDateTime date);

    /** Count successful (non-error) operations for a given operation type. */
    long countByOperationAndIsErrorFalse(String operation);

    /** All measurements that resulted in an error. */
    List<QuantityMeasurementEntity> findByIsErrorTrue();

    // ── Custom JPQL query ────────────────────────────────────────────────────

    /**
     * Finds all successful (non-error) operations of the given type.
     * Uses JPQL — "e" is an alias for QuantityMeasurementEntity.
     */
    @Query("SELECT e FROM QuantityMeasurementEntity e " +
           "WHERE e.operation = :operation AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulOperations(
            @Param("operation") String operation);
}
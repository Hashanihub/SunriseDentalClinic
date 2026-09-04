package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Treatment;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Treatment entity.
 */
public interface TreatmentDAO {

    /**
     * Save a new treatment.
     * @param treatment The treatment to save
     * @return The saved treatment with generated ID
     * @throws DatabaseException if database error occurs
     */
    Treatment save(Treatment treatment) throws DatabaseException;

    /**
     * Find treatment by ID.
     * @param treatmentId The treatment ID
     * @return Optional containing treatment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Treatment> findById(int treatmentId) throws DatabaseException;

    /**
     * Find treatment by code.
     * @param treatmentCode The treatment code
     * @return Optional containing treatment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Treatment> findByCode(String treatmentCode) throws DatabaseException;

    /**
     * Find treatments by name (partial match).
     * @param name The name to search
     * @return List of matching treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> findByName(String name) throws DatabaseException;

    /**
     * Get all treatments.
     * @return List of all treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> findAll() throws DatabaseException;

    /**
     * Get all active treatments.
     * @return List of active treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> findAllActive() throws DatabaseException;

    /**
     * Update treatment.
     * @param treatment The treatment to update
     * @return Updated treatment
     * @throws DatabaseException if database error occurs
     */
    Treatment update(Treatment treatment) throws DatabaseException;

    /**
     * Delete treatment by ID (soft delete - set active to false).
     * @param treatmentId The treatment ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int treatmentId) throws DatabaseException;

    /**
     * Check if treatment code exists.
     * @param treatmentCode The treatment code
     * @return true if treatment code exists
     * @throws DatabaseException if database error occurs
     */
    boolean treatmentCodeExists(String treatmentCode) throws DatabaseException;
}
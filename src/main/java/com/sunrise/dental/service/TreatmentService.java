package com.sunrise.dental.service;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Treatment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Treatment service interface.
 */
public interface TreatmentService {

    /**
     * Add a new treatment.
     * @param treatment The treatment to add
     * @return The added treatment
     * @throws ValidationException if validation fails
     * @throws DatabaseException if database error occurs
     */
    Treatment addTreatment(Treatment treatment) throws ValidationException, DatabaseException;

    /**
     * Get treatment by ID.
     * @param treatmentId The treatment ID
     * @return The treatment
     * @throws ResourceNotFoundException if treatment not found
     * @throws DatabaseException if database error occurs
     */
    Treatment getTreatmentById(int treatmentId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get treatment by code.
     * @param treatmentCode The treatment code
     * @return The treatment
     * @throws ResourceNotFoundException if treatment not found
     * @throws DatabaseException if database error occurs
     */
    Treatment getTreatmentByCode(String treatmentCode) throws ResourceNotFoundException, DatabaseException;

    /**
     * Search treatments by name.
     * @param name The name to search
     * @return List of matching treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> searchTreatmentsByName(String name) throws DatabaseException;

    /**
     * Get all treatments.
     * @return List of all treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> getAllTreatments() throws DatabaseException;

    /**
     * Get all active treatments.
     * @return List of active treatments
     * @throws DatabaseException if database error occurs
     */
    List<Treatment> getActiveTreatments() throws DatabaseException;

    /**
     * Update treatment.
     * @param treatment The treatment to update
     * @return Updated treatment
     * @throws ValidationException if validation fails
     * @throws ResourceNotFoundException if treatment not found
     * @throws DatabaseException if database error occurs
     */
    Treatment updateTreatment(Treatment treatment) throws ValidationException, ResourceNotFoundException, DatabaseException;

    /**
     * Deactivate treatment (soft delete).
     * @param treatmentId The treatment ID
     * @return true if deactivated
     * @throws ResourceNotFoundException if treatment not found
     * @throws DatabaseException if database error occurs
     */
    boolean deactivateTreatment(int treatmentId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Calculate total cost (treatment cost + consultation fee).
     * @param treatmentId The treatment ID
     * @return Total cost
     * @throws ResourceNotFoundException if treatment not found
     * @throws DatabaseException if database error occurs
     */
    BigDecimal calculateTotalCost(int treatmentId) throws ResourceNotFoundException, DatabaseException;
}
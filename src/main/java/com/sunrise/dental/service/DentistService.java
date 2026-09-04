package com.sunrise.dental.service;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.enums.AvailabilityStatus;

import java.util.List;

/**
 * Dentist service interface.
 */
public interface DentistService {

    /**
     * Add a new dentist.
     * @param dentist The dentist to add
     * @return The added dentist
     * @throws ValidationException if validation fails
     * @throws DatabaseException if database error occurs
     */
    Dentist addDentist(Dentist dentist) throws ValidationException, DatabaseException;

    /**
     * Get dentist by ID.
     * @param dentistId The dentist ID
     * @return The dentist
     * @throws ResourceNotFoundException if dentist not found
     * @throws DatabaseException if database error occurs
     */
    Dentist getDentistById(int dentistId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get dentist by dentist number.
     * @param dentistNumber The dentist number
     * @return The dentist
     * @throws ResourceNotFoundException if dentist not found
     * @throws DatabaseException if database error occurs
     */
    Dentist getDentistByNumber(String dentistNumber) throws ResourceNotFoundException, DatabaseException;

    /**
     * Search dentists by name.
     * @param name The name to search
     * @return List of matching dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> searchDentistsByName(String name) throws DatabaseException;

    /**
     * Get dentists by specialization.
     * @param specialization The specialization
     * @return List of dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> getDentistsBySpecialization(String specialization) throws DatabaseException;

    /**
     * Get all dentists.
     * @return List of all dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> getAllDentists() throws DatabaseException;

    /**
     * Get all active dentists.
     * @return List of active dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> getActiveDentists() throws DatabaseException;

    /**
     * Get available dentists.
     * @return List of available dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> getAvailableDentists() throws DatabaseException;

    /**
     * Update dentist.
     * @param dentist The dentist to update
     * @return Updated dentist
     * @throws ValidationException if validation fails
     * @throws ResourceNotFoundException if dentist not found
     * @throws DatabaseException if database error occurs
     */
    Dentist updateDentist(Dentist dentist) throws ValidationException, ResourceNotFoundException, DatabaseException;

    /**
     * Update dentist availability.
     * @param dentistId The dentist ID
     * @param status The new availability status
     * @return true if updated
     * @throws ResourceNotFoundException if dentist not found
     * @throws DatabaseException if database error occurs
     */
    boolean updateDentistAvailability(int dentistId, AvailabilityStatus status)
            throws ResourceNotFoundException, DatabaseException;

    /**
     * Deactivate dentist (soft delete).
     * @param dentistId The dentist ID
     * @return true if deactivated
     * @throws ResourceNotFoundException if dentist not found
     * @throws DatabaseException if database error occurs
     */
    boolean deactivateDentist(int dentistId) throws ResourceNotFoundException, DatabaseException;
}
package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.enums.AvailabilityStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Dentist entity.
 */
public interface DentistDAO {

    /**
     * Save a new dentist.
     * @param dentist The dentist to save
     * @return The saved dentist with generated ID
     * @throws DatabaseException if database error occurs
     */
    Dentist save(Dentist dentist) throws DatabaseException;

    /**
     * Find dentist by ID.
     * @param dentistId The dentist ID
     * @return Optional containing dentist if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Dentist> findById(int dentistId) throws DatabaseException;

    /**
     * Find dentist by dentist number.
     * @param dentistNumber The dentist number
     * @return Optional containing dentist if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Dentist> findByDentistNumber(String dentistNumber) throws DatabaseException;

    /**
     * Find dentists by name (partial match).
     * @param name The name to search
     * @return List of matching dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> findByName(String name) throws DatabaseException;

    /**
     * Find dentists by specialization.
     * @param specialization The specialization
     * @return List of dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> findBySpecialization(String specialization) throws DatabaseException;

    /**
     * Find available dentists.
     * @return List of available dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> findAvailableDentists() throws DatabaseException;

    /**
     * Get all dentists.
     * @return List of all dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> findAll() throws DatabaseException;

    /**
     * Get all active dentists.
     * @return List of active dentists
     * @throws DatabaseException if database error occurs
     */
    List<Dentist> findAllActive() throws DatabaseException;

    /**
     * Update dentist.
     * @param dentist The dentist to update
     * @return Updated dentist
     * @throws DatabaseException if database error occurs
     */
    Dentist update(Dentist dentist) throws DatabaseException;

    /**
     * Update dentist availability status.
     * @param dentistId The dentist ID
     * @param status The new status
     * @return true if updated successfully
     * @throws DatabaseException if database error occurs
     */
    boolean updateAvailabilityStatus(int dentistId, AvailabilityStatus status) throws DatabaseException;

    /**
     * Delete dentist by ID (soft delete - set active to false).
     * @param dentistId The dentist ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int dentistId) throws DatabaseException;
}
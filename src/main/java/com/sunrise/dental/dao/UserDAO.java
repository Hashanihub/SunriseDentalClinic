package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity.
 */
public interface UserDAO {

    /**
     * Save a new user.
     * @param user The user to save
     * @return The saved user with generated ID
     * @throws DatabaseException if database error occurs
     */
    User save(User user) throws DatabaseException;

    /**
     * Find user by ID.
     * @param userId The user ID
     * @return Optional containing user if found
     * @throws DatabaseException if database error occurs
     */
    Optional<User> findById(int userId) throws DatabaseException;

    /**
     * Find user by username.
     * @param username The username
     * @return Optional containing user if found
     * @throws DatabaseException if database error occurs
     */
    Optional<User> findByUsername(String username) throws DatabaseException;

    /**
     * Find user by email.
     * @param email The email
     * @return Optional containing user if found
     * @throws DatabaseException if database error occurs
     */
    Optional<User> findByEmail(String email) throws DatabaseException;

    /**
     * Get all users.
     * @return List of all users
     * @throws DatabaseException if database error occurs
     */
    List<User> findAll() throws DatabaseException;

    /**
     * Get users by role.
     * @param role The user role
     * @return List of users with the given role
     * @throws DatabaseException if database error occurs
     */
    List<User> findByRole(UserRole role) throws DatabaseException;

    /**
     * Update user.
     * @param user The user to update
     * @return Updated user
     * @throws DatabaseException if database error occurs
     */
    User update(User user) throws DatabaseException;

    /**
     * Delete user by ID (soft delete - set active to false).
     * @param userId The user ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int userId) throws DatabaseException;

    /**
     * Check if username exists.
     * @param username The username
     * @return true if username exists
     * @throws DatabaseException if database error occurs
     */
    boolean usernameExists(String username) throws DatabaseException;

    /**
     * Check if email exists.
     * @param email The email
     * @return true if email exists
     * @throws DatabaseException if database error occurs
     */
    boolean emailExists(String email) throws DatabaseException;

    /**
     * Update user password.
     * @param userId The user ID
     * @param newPasswordHash The new password hash
     * @return true if updated successfully
     * @throws DatabaseException if database error occurs
     */
    boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException;
}
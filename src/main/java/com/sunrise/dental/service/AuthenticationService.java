package com.sunrise.dental.service;

import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;

import jakarta.servlet.http.HttpSession;

/**
 * Service interface for authentication operations.
 */
public interface AuthenticationService {

    /**
     * Authenticate user with username and password.
     * @param username The username
     * @param password The plain text password
     * @return Authenticated user
     * @throws AuthenticationException if authentication fails
     * @throws DatabaseException if database error occurs
     */
    User login(String username, String password) throws AuthenticationException, DatabaseException;

    /**
     * Logout user - invalidate session.
     * @param session The HTTP session
     */
    void logout(HttpSession session);

    /**
     * Check if user is authenticated.
     * @param session The HTTP session
     * @return true if authenticated
     */
    boolean isAuthenticated(HttpSession session);

    /**
     * Get current user from session.
     * @param session The HTTP session
     * @return Current user or null
     */
    User getCurrentUser(HttpSession session);

    /**
     * Check if user has required role.
     * @param session The HTTP session
     * @param requiredRole The required role
     * @return true if user has the role
     */
    boolean hasRole(HttpSession session, UserRole requiredRole);

    /**
     * Change user password.
     * @param userId The user ID
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return true if password changed successfully
     * @throws AuthenticationException if old password is incorrect
     * @throws DatabaseException if database error occurs
     */
    boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, DatabaseException;

    /**
     * Hash a password using BCrypt.
     * @param plainPassword The plain text password
     * @return Hashed password
     */
    String hashPassword(String plainPassword);

    /**
     * Verify a password against a hash.
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password
     * @return true if passwords match
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);
}
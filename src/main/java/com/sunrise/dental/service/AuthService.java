package com.sunrise.dental.service;

import com.sunrise.dental.dto.LoginRequestDTO;
import com.sunrise.dental.dto.LoginResponseDTO;
import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;

/**
 * Authentication service interface.
 */
public interface AuthService {

    /**
     * Authenticate a user login.
     * @param loginRequest The login request
     * @return Login response with user details
     * @throws AuthenticationException if authentication fails
     * @throws DatabaseException if database error occurs
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest) throws AuthenticationException, DatabaseException;

    /**
     * Logout a user (invalidate session).
     * @param userId The user ID
     */
    void logout(int userId);

    /**
     * Validate user session.
     * @param userId The user ID
     * @return true if session is valid
     */
    boolean validateSession(int userId);

    /**
     * Get current user.
     * @param userId The user ID
     * @return The user
     * @throws AuthenticationException if user not found
     * @throws DatabaseException if database error occurs
     */
    User getCurrentUser(int userId) throws AuthenticationException, DatabaseException;

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
     * Check if user has role.
     * @param user The user
     * @param requiredRole The required role
     * @return true if user has the role
     */
    boolean hasRole(User user, String requiredRole);

    /**
     * Check if user is active.
     * @param user The user
     * @return true if user is active
     */
    boolean isUserActive(User user);
}
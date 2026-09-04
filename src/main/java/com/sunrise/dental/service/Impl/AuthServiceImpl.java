package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.dao.Impl.UserDAOImpl;
import com.sunrise.dental.dto.LoginRequestDTO;
import com.sunrise.dental.dto.LoginResponseDTO;
import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;
import com.sunrise.dental.service.AuthService;
import com.sunrise.dental.util.PasswordUtil;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Implementation of AuthService interface.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);
    private final UserDAO userDAO;

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) throws AuthenticationException, DatabaseException {
        try {
            // Validate input
            ValidationUtil.validateUsername(loginRequest.getUsername());
            ValidationUtil.validatePassword(loginRequest.getPassword());

            // Find user by username
            Optional<User> userOptional = userDAO.findByUsername(loginRequest.getUsername());

            if (userOptional.isEmpty()) {
                logger.warn("Login failed: User not found - {}", loginRequest.getUsername());
                throw new AuthenticationException("Invalid username or password");
            }

            User user = userOptional.get();

            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed: User is inactive - {}", loginRequest.getUsername());
                throw new AuthenticationException("Account is inactive. Please contact administrator.");
            }

            // Debug logging
            logger.info("Stored hash: {}", user.getPasswordHash());
            logger.info("Password length: {}", loginRequest.getPassword().length());

            // Verify password - Use BCrypt directly
            boolean passwordMatch = BCrypt.checkpw(loginRequest.getPassword(), user.getPasswordHash());

            logger.info("Password match: {}", passwordMatch);

            if (!passwordMatch) {
                logger.warn("Login failed: Invalid password for user - {}", loginRequest.getUsername());
                throw new AuthenticationException("Invalid username or password");
            }

            logger.info("Login successful: {}", loginRequest.getUsername());
            return LoginResponseDTO.success(user);

        } catch (ValidationException e) {
            logger.warn("Login validation failed: {}", e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }
    @Override
    public void logout(int userId) {
        logger.info("User logged out: {}", userId);
        // Session invalidation is handled by the servlet
    }

    @Override
    public boolean validateSession(int userId) {
        try {
            Optional<User> user = userDAO.findById(userId);
            return user.isPresent() && user.get().isActive();
        } catch (DatabaseException e) {
            logger.error("Error validating session for user: {}", userId, e);
            return false;
        }
    }

    @Override
    public User getCurrentUser(int userId) throws AuthenticationException, DatabaseException {
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new AuthenticationException("User not found");
        }
        if (!user.get().isActive()) {
            throw new AuthenticationException("User account is inactive");
        }
        return user.get();
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, DatabaseException {

        try {
            // Validate new password
            ValidationUtil.validatePassword(newPassword);

            // Get user
            Optional<User> userOptional = userDAO.findById(userId);
            if (userOptional.isEmpty()) {
                throw new AuthenticationException("User not found");
            }

            User user = userOptional.get();

            // Verify old password
            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Current password is incorrect");
            }

            // Hash new password
            String newHash = PasswordUtil.hashPassword(newPassword);

            // Update password
            boolean updated = userDAO.updatePassword(userId, newHash);

            if (updated) {
                logger.info("Password changed for user: {}", userId);
                return true;
            }

            throw new DatabaseException("Failed to update password");

        } catch (ValidationException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public boolean hasRole(User user, String requiredRole) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        // Admin has all permissions
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }
        try {
            UserRole required = UserRole.fromString(requiredRole);
            return user.getRole() == required;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean isUserActive(User user) {
        return user != null && user.isActive();
    }
}
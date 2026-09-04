package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.dao.Impl.UserDAOImpl;
import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;
import com.sunrise.dental.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

/**
 * Implementation of AuthenticationService.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);
    private static final String SESSION_USER_KEY = "user";
    private static final String SESSION_ROLE_KEY = "role";

    private final UserDAO userDAO;

    public AuthenticationServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public User login(String username, String password)
            throws AuthenticationException, DatabaseException {

        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password is required");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());

        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found - {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            logger.warn("Login failed: User account is inactive - {}", username);
            throw new AuthenticationException("Account is deactivated. Please contact admin.");
        }

        if (!verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for user - {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        logger.info("User logged in successfully: {} ({})", username, user.getRole());
        return user;
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            logger.info("User logged out: {}", username);
        }
    }

    @Override
    public boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    @Override
    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SESSION_USER_KEY);
    }

    @Override
    public boolean hasRole(HttpSession session, UserRole requiredRole) {
        User user = getCurrentUser(session);
        if (user == null) {
            return false;
        }
        return user.getRole() == requiredRole;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, DatabaseException {

        Optional<User> userOpt = userDAO.findById(userId);

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("User not found");
        }

        User user = userOpt.get();

        if (!verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new AuthenticationException("New password cannot be empty");
        }

        if (newPassword.length() < 6) {
            throw new AuthenticationException("New password must be at least 6 characters");
        }

        String newHashedPassword = hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHashedPassword);
    }

    @Override
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
}
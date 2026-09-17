package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.dao.Impl.UserDAOImpl;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.service.UserService;
import com.sunrise.dental.util.PasswordUtil;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public User createUser(User user) throws ValidationException, DatabaseException {
        logger.info("Creating new user: {}", user.getUsername());

        // Validate
        ValidationUtil.validateRequired(user.getUsername(), "Username");
        ValidationUtil.validateRequired(user.getFullName(), "Full Name");
        ValidationUtil.validateRequired(user.getRole(), "Role");

        // Check if username exists
        if (userDAO.usernameExists(user.getUsername())) {
            throw new ValidationException("Username already exists. Please choose another.");
        }

        // Default password if not provided: "password123"
        String rawPassword = "password123";
        user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        user.setActive(true);

        User saved = userDAO.save(user);
        logger.info("User created successfully: {}", saved.getUsername());
        return saved;
    }

    @Override
    public User getUserById(int userId) throws ResourceNotFoundException, DatabaseException {
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User", String.valueOf(userId));
        }
        return user.get();
    }

    @Override
    public List<User> getAllUsers() throws DatabaseException {
        return userDAO.findAll();
    }

    @Override
    public User updateUser(User user) throws ValidationException, ResourceNotFoundException, DatabaseException {
        logger.info("Updating user: {}", user.getUsername());

        Optional<User> existing = userDAO.findById(user.getUserId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("User", String.valueOf(user.getUserId()));
        }

        ValidationUtil.validateRequired(user.getFullName(), "Full Name");
        ValidationUtil.validateRequired(user.getRole(), "Role");

        // Preserve existing password hash
        user.setPasswordHash(existing.get().getPasswordHash());

        User updated = userDAO.update(user);
        logger.info("User updated successfully: {}", updated.getUsername());
        return updated;
    }

    @Override
    public boolean deactivateUser(int userId) throws ResourceNotFoundException, DatabaseException {
        Optional<User> user = userDAO.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User", String.valueOf(userId));
        }
        return userDAO.delete(userId);
    }

    @Override
    public boolean usernameExists(String username) throws DatabaseException {
        return userDAO.usernameExists(username);
    }
}
package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserDAO interface using JDBC.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public UserDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public User save(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole().name());
            stmt.setBoolean(6, user.isActive());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }

            logger.info("User saved successfully: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error saving user: {}", user.getUsername(), e);
            throw new DatabaseException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(int userId) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", userId, e);
            throw new DatabaseException("Failed to find user by ID", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new DatabaseException("Failed to find user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseException("Failed to find user by email", e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

            return users;

        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new DatabaseException("Failed to find all users", e);
        }
    }

    @Override
    public List<User> findByRole(UserRole role) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE role = ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }

            return users;

        } catch (SQLException e) {
            logger.error("Error finding users by role: {}", role, e);
            throw new DatabaseException("Failed to find users by role", e);
        }
    }

    @Override
    public User update(User user) throws DatabaseException {
        String sql = "UPDATE users SET username = ?, full_name = ?, email = ?, role = ?, active = ? " +
                "WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole().name());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getUserId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating user failed, no rows affected.");
            }

            logger.info("User updated successfully: {}", user.getUsername());
            return user;

        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUsername(), e);
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(int userId) throws DatabaseException {
        String sql = "UPDATE users SET active = false WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();

            logger.info("User soft deleted: {}", userId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new DatabaseException("Failed to delete user", e);
        }
    }

    @Override
    public boolean usernameExists(String username) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking username existence: {}", username, e);
            throw new DatabaseException("Failed to check username existence", e);
        }
    }

    @Override
    public boolean emailExists(String email) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking email existence: {}", email, e);
            throw new DatabaseException("Failed to check email existence", e);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Password updated for user: {}", userId);
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error updating password for user: {}", userId, e);
            throw new DatabaseException("Failed to update password", e);
        }
    }

    /**
     * Map ResultSet to User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.fromString(rs.getString("role")));
        user.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}
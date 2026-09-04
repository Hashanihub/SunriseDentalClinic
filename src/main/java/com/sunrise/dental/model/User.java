package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.UserRole;
import java.time.LocalDateTime;

/**
 * User entity for authentication and authorization.
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public User() {
        this.active = true;
    }

    // Parameterized constructor
    public User(int userId, String username, String passwordHash, String fullName,
                String email, UserRole role, boolean active,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder pattern for easy object creation
    public static class Builder {
        private User user = new User();

        public Builder userId(int userId) {
            user.userId = userId;
            return this;
        }

        public Builder username(String username) {
            user.username = username;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            user.passwordHash = passwordHash;
            return this;
        }

        public Builder fullName(String fullName) {
            user.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            user.email = email;
            return this;
        }

        public Builder role(UserRole role) {
            user.role = role;
            return this;
        }

        public Builder active(boolean active) {
            user.active = active;
            return this;
        }

        public User build() {
            return user;
        }
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
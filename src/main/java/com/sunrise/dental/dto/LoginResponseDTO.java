package com.sunrise.dental.dto;

import com.sunrise.dental.model.User;

/**
 * Data Transfer Object for login responses.
 */
public class LoginResponseDTO {

    private boolean success;
    private String message;
    private Integer userId;
    private String username;
    private String fullName;
    private String role;
    private String token;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponseDTO(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        if (user != null) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
            this.role = user.getRole() != null ? user.getRole().name() : null;
        }
    }

    public static LoginResponseDTO success(User user) {
        return new LoginResponseDTO(true, "Login successful", user);
    }

    public static LoginResponseDTO failure(String message) {
        return new LoginResponseDTO(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
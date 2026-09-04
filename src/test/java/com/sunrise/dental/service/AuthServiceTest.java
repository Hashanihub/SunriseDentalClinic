package com.sunrise.dental.service;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.dto.LoginRequestDTO;
import com.sunrise.dental.dto.LoginResponseDTO;
import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;
import com.sunrise.dental.service.Impl.AuthServiceImpl;
import com.sunrise.dental.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequestDTO validLoginRequest;
    private LoginRequestDTO invalidLoginRequest;

    @BeforeEach
    void setUp() {
        String hashedPassword = PasswordUtil.hashPassword("password123");

        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(hashedPassword);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.RECEPTIONIST);
        testUser.setActive(true);

        validLoginRequest = new LoginRequestDTO("testuser", "password123");
        invalidLoginRequest = new LoginRequestDTO("wronguser", "wrongpass");
    }

    @Test
    void login_ShouldSucceed_WhenCredentialsAreValid() throws Exception {
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        LoginResponseDTO response = authService.login(validLoginRequest);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(testUser.getUserId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getRole().name(), response.getRole());

        verify(userDAO).findByUsername("testuser");
    }

    @Test
    void login_ShouldFail_WhenUserNotFound() throws Exception {
        when(userDAO.findByUsername("wronguser")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> {
            authService.login(invalidLoginRequest);
        });

        verify(userDAO).findByUsername("wronguser");
    }

    @Test
    void login_ShouldFail_WhenPasswordIsIncorrect() throws Exception {
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        LoginRequestDTO wrongPasswordRequest = new LoginRequestDTO("testuser", "wrongpassword");

        assertThrows(AuthenticationException.class, () -> {
            authService.login(wrongPasswordRequest);
        });

        verify(userDAO).findByUsername("testuser");
    }

    @Test
    void login_ShouldFail_WhenUserIsInactive() throws Exception {
        testUser.setActive(false);
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> {
            authService.login(validLoginRequest);
        });

        verify(userDAO).findByUsername("testuser");
    }

    @Test
    void login_ShouldFail_WhenUsernameIsEmpty() throws Exception {
        LoginRequestDTO emptyUsernameRequest = new LoginRequestDTO("", "password");

        assertThrows(AuthenticationException.class, () -> {
            authService.login(emptyUsernameRequest);
        });

        verify(userDAO, never()).findByUsername(anyString());
    }

    @Test
    void login_ShouldFail_WhenPasswordIsEmpty() throws Exception {
        LoginRequestDTO emptyPasswordRequest = new LoginRequestDTO("testuser", "");

        assertThrows(AuthenticationException.class, () -> {
            authService.login(emptyPasswordRequest);
        });

        verify(userDAO, never()).findByUsername(anyString());
    }

    @Test
    void login_ShouldThrowDatabaseException_WhenDAOFails() throws Exception {
        when(userDAO.findByUsername("testuser")).thenThrow(new DatabaseException("DB Error"));

        assertThrows(DatabaseException.class, () -> {
            authService.login(validLoginRequest);
        });

        verify(userDAO).findByUsername("testuser");
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenUserExists() throws Exception {
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        User user = authService.getCurrentUser(1);

        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());
        assertEquals(testUser.getUsername(), user.getUsername());

        verify(userDAO).findById(1);
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() throws Exception {
        when(userDAO.findById(999)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> {
            authService.getCurrentUser(999);
        });

        verify(userDAO).findById(999);
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserIsInactive() throws Exception {
        testUser.setActive(false);
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> {
            authService.getCurrentUser(1);
        });

        verify(userDAO).findById(1);
    }

    @Test
    void changePassword_ShouldSucceed_WhenOldPasswordIsCorrect() throws Exception {
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));
        when(userDAO.updatePassword(1, PasswordUtil.hashPassword("newPassword123"))).thenReturn(true);

        boolean result = authService.changePassword(1, "password123", "newPassword123");

        assertTrue(result);
        verify(userDAO).findById(1);
        verify(userDAO).updatePassword(eq(1), anyString());
    }

    @Test
    void changePassword_ShouldFail_WhenOldPasswordIsIncorrect() throws Exception {
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> {
            authService.changePassword(1, "wrongpassword", "newPassword123");
        });

        verify(userDAO).findById(1);
        verify(userDAO, never()).updatePassword(anyInt(), anyString());
    }

    @Test
    void changePassword_ShouldFail_WhenNewPasswordIsTooShort() throws Exception {
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> {
            authService.changePassword(1, "password123", "123");
        });

        verify(userDAO).findById(1);
        verify(userDAO, never()).updatePassword(anyInt(), anyString());
    }

    @Test
    void hasRole_ShouldReturnTrue_WhenUserHasRole() {
        assertTrue(authService.hasRole(testUser, "RECEPTIONIST"));
        assertTrue(authService.hasRole(testUser, "ADMIN")); // Admin has all permissions
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        assertFalse(authService.hasRole(testUser, "DOCTOR"));
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserIsNull() {
        assertFalse(authService.hasRole(null, "ADMIN"));
    }

    @Test
    void isUserActive_ShouldReturnTrue_WhenUserIsActive() {
        assertTrue(authService.isUserActive(testUser));
    }

    @Test
    void isUserActive_ShouldReturnFalse_WhenUserIsInactive() {
        testUser.setActive(false);
        assertFalse(authService.isUserActive(testUser));
    }

    @Test
    void isUserActive_ShouldReturnFalse_WhenUserIsNull() {
        assertFalse(authService.isUserActive(null));
    }

    @Test
    void validateSession_ShouldReturnTrue_WhenSessionIsValid() throws Exception {
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        boolean result = authService.validateSession(1);

        assertTrue(result);
        verify(userDAO).findById(1);
    }

    @Test
    void validateSession_ShouldReturnFalse_WhenUserNotFound() throws Exception {
        when(userDAO.findById(999)).thenReturn(Optional.empty());

        boolean result = authService.validateSession(999);

        assertFalse(result);
        verify(userDAO).findById(999);
    }

    @Test
    void validateSession_ShouldReturnFalse_WhenUserIsInactive() throws Exception {
        testUser.setActive(false);
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        boolean result = authService.validateSession(1);

        assertFalse(result);
        verify(userDAO).findById(1);
    }

    @Test
    void validateSession_ShouldReturnFalse_WhenDatabaseExceptionOccurs() throws Exception {
        when(userDAO.findById(1)).thenThrow(new DatabaseException("DB Error"));

        boolean result = authService.validateSession(1);

        assertFalse(result);
        verify(userDAO).findById(1);
    }
}
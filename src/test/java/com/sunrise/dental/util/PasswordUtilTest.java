package com.sunrise.dental.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class.
 */
class PasswordUtilTest {

    @Test
    void hashPassword_ShouldReturnHashedPassword() {
        String password = "mySecretPassword";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("$2a$")); // BCrypt format
    }

    @Test
    void hashPassword_ShouldThrowException_WhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null);
        });
    }

    @Test
    void hashPassword_ShouldThrowException_WhenPasswordIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        });
    }

    @Test
    void verifyPassword_ShouldReturnTrue_WhenPasswordMatches() {
        String password = "mySecretPassword";
        String hash = PasswordUtil.hashPassword(password);

        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    void verifyPassword_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        String password = "mySecretPassword";
        String hash = PasswordUtil.hashPassword(password);

        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hash));
    }

    @Test
    void verifyPassword_ShouldReturnFalse_WhenHashIsNull() {
        assertFalse(PasswordUtil.verifyPassword("password", null));
    }

    @Test
    void verifyPassword_ShouldReturnFalse_WhenPasswordIsNull() {
        String hash = PasswordUtil.hashPassword("password");
        assertFalse(PasswordUtil.verifyPassword(null, hash));
    }

    @Test
    void verifyPassword_ShouldReturnFalse_WhenBothAreNull() {
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }

    @Test
    void verifyPassword_ShouldReturnFalse_WhenHashIsInvalid() {
        assertFalse(PasswordUtil.verifyPassword("password", "invalidHash"));
    }

    @Test
    void hashPassword_ShouldProduceDifferentHashesForSamePassword() {
        String password = "samePassword";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertNotEquals(hash1, hash2);
        // Both should verify the password
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }

    @Test
    void hashPassword_ShouldWorkWithSpecialCharacters() {
        String password = "P@ssw0rd!@#$%^&*()";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    void hashPassword_ShouldWorkWithLongPassword() {
        String password = "a".repeat(100);
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    void needsRehash_ShouldReturnFalse_ForValidBCryptHash() {
        String hash = PasswordUtil.hashPassword("password");
        assertFalse(PasswordUtil.needsRehash(hash));
    }

    @Test
    void needsRehash_ShouldReturnTrue_WhenHashIsNull() {
        assertTrue(PasswordUtil.needsRehash(null));
    }
}
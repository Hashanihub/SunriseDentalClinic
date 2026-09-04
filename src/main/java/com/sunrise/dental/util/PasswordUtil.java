package com.sunrise.dental.util;

import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordUtil {

    private static final Logger logger = LogManager.getLogger(PasswordUtil.class);
    private static final int BCRYPT_WORKLOAD = 10;

    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hash a password using BCrypt.
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));
    }

    /**
     * Verify a password against a hash.
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            logger.error("BCrypt verification error", e);
            return false;
        }
    }

    /**
     * Check if a hash needs to be re-hashed (to upgrade to stronger hash).
     * @param hashedPassword The hashed password
     * @return true if re-hash is needed
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }
        // BCrypt work factor is embedded in the hash
        // This is a simple check - in real systems you might check the work factor
        return false;
    }
}
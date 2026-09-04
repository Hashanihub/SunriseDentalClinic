package com.sunrise.dental.config;

import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application configuration manager.
 * Loads configuration from properties files and provides access to configuration values.
 */
public class AppConfig {

    private static final Logger logger = LogManager.getLogger(AppConfig.class);
    private static AppConfig instance;
    private final Properties properties;

    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Get the singleton instance of AppConfig.
     * @return AppConfig instance
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Load properties from the configuration file.
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Configuration loaded successfully");
            } else {
                logger.warn("application.properties not found, using defaults");
                // Set default values
                setDefaults();
            }
        } catch (Exception e) {
            logger.error("Error loading configuration", e);
            setDefaults();
        }
    }

    /**
     * Set default configuration values.
     */
    private void setDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/sunrise_dental_db");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.pool.size", "10");
        properties.setProperty("app.name", "Sunrise Dental Clinic");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("session.timeout", "30");
    }

    /**
     * Get a configuration property value.
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get a configuration property value with a default fallback.
     * @param key The property key
     * @param defaultValue The default value if property is not found
     * @return The property value or default
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get a property as an integer.
     * @param key The property key
     * @return The integer value, or 0 if not found
     */
    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key, "0"));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property: {}", key);
            return 0;
        }
    }

    /**
     * Get a property as a boolean.
     * @param key The property key
     * @return The boolean value, or false if not found
     */
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(properties.getProperty(key, "false"));
    }
}
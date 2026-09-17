package com.sunrise.dental.config;

import com.sunrise.dental.exception.DatabaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager using Singleton pattern.
 * Provides a single point of access for database connections.
 */
public class DatabaseConnection {

    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;
    private static Connection connection;
    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    /**
     * Private constructor for Singleton pattern.
     * Loads configuration from AppConfig.
     */
    private DatabaseConnection() {
        AppConfig config = AppConfig.getInstance();
        this.url = config.getProperty("db.url", "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&serverTimezone=Asia/Colombo&allowPublicKeyRetrieval=true");
        this.username = config.getProperty("db.username", "root");
        this.password = config.getProperty("db.password", "SunriseDental@2024");
        this.driver = config.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
            logger.info("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load database driver", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    /**
     * Get the singleton instance of DatabaseConnection.
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get a database connection.
     * @return Connection object
     * @throws DatabaseException if connection fails
     */
    public Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                logger.info("Database connection established successfully");
            }
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw new DatabaseException("Failed to connect to database", e);
        }
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }

    /**
     * Test the database connection.
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get a new connection (not shared).
     * @return new Connection
     * @throws DatabaseException if connection fails
     */
    public Connection getNewConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("Failed to get new database connection", e);
            throw new DatabaseException("Failed to connect to database", e);
        }
    }

    /**
     * Rollback a transaction.
     * @param conn The connection
     */
    public void rollback(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                logger.debug("Transaction rolled back");
            }
        } catch (SQLException e) {
            logger.error("Failed to rollback transaction", e);
        }
    }

    /**
     * Close connection quietly (no exception thrown).
     * @param conn The connection to close
     */
    public void closeQuietly(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.warn("Failed to close connection quietly", e);
        }
    }

    /**
     * Get database URL.
     * @return Database URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get database username.
     * @return Username
     */
    public String getUsername() {
        return username;
    }
}
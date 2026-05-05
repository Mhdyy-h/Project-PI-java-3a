package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;
    private static Connection testConnection = null;
    private static boolean testMode = false;
    private static Properties properties = new Properties();

    static {
        try {
            InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
            if (input != null) {
                properties.load(input);
                input.close();
            } else {
                System.out.println("Sorry, unable to find config.properties");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (testMode && testConnection != null) {
            return testConnection;
        }
        try {
            if (connection == null || connection.isClosed()) {
                String url      = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");
                String driver   = properties.getProperty("db.driver");

                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connection established successfully!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Set test connection for unit testing
     * @param conn Test database connection
     */
    public static void setTestConnection(Connection conn) {
        testConnection = conn;
        testMode = (conn != null);
    }

    /**
     * Check if running in test mode
     * @return true if using test connection
     */
    public static boolean isTestMode() {
        return testMode;
    }

    /**
     * Reset to production mode
     */
    public static void resetTestMode() {
        testMode = false;
        testConnection = null;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}

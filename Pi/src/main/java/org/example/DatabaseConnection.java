package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Properties properties = new Properties();

    static {
        try {
            InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            if (input != null) {
                properties.load(input);
                input.close();
            } else {
                System.err.println("❌ config.properties not found!");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String url      = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        String driver   = properties.getProperty("db.driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found: " + e.getMessage());
        }

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("✅ DB connection opened.");
        return conn;
    }
}
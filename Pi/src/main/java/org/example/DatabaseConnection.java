package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;
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
        try {
            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {
                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");
                String driver = properties.getProperty("db.driver");

                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("✅ Database connection established successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
            System.err.println("   Make sure mysql-connector-j is in your dependencies.");
            connection = null;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            if (e.getMessage().contains("Unknown database")) {
                System.err.println("\n╔════════════════════════════════════════════════════════════╗");
                System.err.println("║  LA BASE DE DONNÉES 'biosync' N'EXISTE PAS !              ║");
                System.err.println("╚════════════════════════════════════════════════════════════╝");
                System.err.println("\n📋 Pour créer la base de données :");
                System.err.println("   1. Ouvrez un terminal dans le dossier 'database/'");
                System.err.println("   2. Exécutez : setup_database.bat");
                System.err.println("   3. Ou importez manuellement : create_database.sql");
                System.err.println("\n📖 Consultez database/README.md pour plus d'informations.\n");
            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("\n⚠️  Vérifiez vos identifiants MySQL dans config.properties");
            }
            connection = null;
        }
        return connection;
    }

    public static Connection getFreshConnection() {
        try {
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");

            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Fresh connection failed: " + e.getMessage());
            return null;
        }
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

package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
        System.out.println("Testing database connection...");
        

        

            // Show table structure
            try {
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("DESCRIBE utilisateur");
                System.out.println("\n--- Structure of 'utilisateur' table ---");
                while (rs.next()) {
                    System.out.println(rs.getString("Field") + " - " + rs.getString("Type") + " - " + rs.getString("Null") + " - " + rs.getString("Key"));
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

        }
    }

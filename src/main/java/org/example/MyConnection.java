package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    static String url = "jdbc:mysql://localhost:3306/biosync";
    static String user = "root";
    static String pwd = "";

    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(url, user, pwd);
                System.out.println("✅ Connexion reussie !");
            } catch (SQLException e) {
                System.out.println("❌ Erreur : " + e.getMessage());
            }
        }
        return conn;
    }
}
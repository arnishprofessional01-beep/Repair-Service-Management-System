package com.repairservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Aiven Cloud Database URL with SSL disabled verification for smooth cloud connection
    private static final String URL = "jdbc:mysql://mysql-repair-service-repair-service-management-system.c.aivencloud.com:18075/repair_service_db?useSSL=true&trustServerCertificate=true";
    private static final String USER = "avnadmin";
    
    // Aapka actual Aiven password yahan direct daalna:
    private static final String PASSWORD = "AVNS_JBzGOt-s5r1ULrim3z-E";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver Error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Cloud DB Connection Error: " + e.getMessage());
        }
        return conn;
    }
}

package com.repairservice.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    // Aiven Cloud Database Exact JDBC Connection URL
    private static final String URL = "jdbc:mysql://mysql-repair-service-repair-service-management-system.c.aivencloud.com:18075/repair_service_db?sslmode=REQUIRED&useSSL=true&allowPublicKeyRetrieval=true";
    private static final String USER = "avnadmin";
    
    // Yahan apna Aiven wala real password daal
    private static final String PASSWORD = "APNA_AIVEN_PASSWORD_HERE";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println("--- DB CONNECTION ERROR: " + e.getMessage() + " ---");
            e.printStackTrace();
        }
        return conn;
    }
}

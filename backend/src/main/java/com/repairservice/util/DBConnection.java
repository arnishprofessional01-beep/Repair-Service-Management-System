package com.repairservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Aiven Cloud Database Details
    private static final String URL = "jdbc:mysql://mysql-repair-service-repair-service-management-system.c.aivencloud.com:18075/repair_service_db?sslmode=REQUIRED";
    private static final String USER = "avnadmin";
    
    // Aiven Console se copy kiya hua password yahan paste karo:
    private static final String PASSWORD = "AVNS_JBzGOt-s5r1ULrim3z-E";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found in classpath: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Cloud database connection failed: " + e.getMessage());
        }
        return conn;
    }
}
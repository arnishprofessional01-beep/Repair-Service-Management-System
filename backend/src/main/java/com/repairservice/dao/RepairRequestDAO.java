package com.repairservice.dao;

import com.repairservice.model.RepairRequest;
import com.repairservice.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RepairRequestDAO {

    public synchronized String generateNextRequestId() {
        String sql = "SELECT request_id FROM repair_requests ORDER BY request_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString("request_id");
                if (lastId != null && lastId.startsWith("RS")) {
                    String numPart = lastId.substring(2).replaceAll("\\D", "");
                    if (!numPart.isEmpty()) {
                        int num = Integer.parseInt(numPart);
                        return "RS" + (num + 1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating ID: " + e.getMessage());
        }
        return "RS1001";
    }

    public boolean createRequest(RepairRequest req) {
        // EXACT 7 PLACEHOLDERS: 1(req_id), 2(user_id), NULL, 3(service_type), 4(device_name), 5(problem), 6(description), 7(preferred_date), 'Pending'
        String sql = "INSERT INTO repair_requests (request_id, user_id, technician_id, service_type, device_name, problem, description, preferred_date, status) VALUES (?, ?, NULL, ?, ?, ?, ?, ?, 'Pending')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String reqId = (req.getRequestId() != null && !req.getRequestId().trim().isEmpty()) 
                           ? req.getRequestId() : generateNextRequestId();

            // 1. request_id
            stmt.setString(1, reqId);
            // 2. user_id
            stmt.setInt(2, req.getUserId() > 0 ? req.getUserId() : 6);
            // 3. service_type
            stmt.setString(3, (req.getServiceType() != null && !req.getServiceType().trim().isEmpty()) ? req.getServiceType() : "General");
            // 4. device_name
            stmt.setString(4, (req.getDeviceName() != null && !req.getDeviceName().trim().isEmpty()) ? req.getDeviceName() : "Device");
            // 5. problem
            stmt.setString(5, (req.getProblem() != null && !req.getProblem().trim().isEmpty()) ? req.getProblem() : "Issue");
            // 6. description
            stmt.setString(6, req.getDescription() != null ? req.getDescription() : "");

            // 7. preferred_date
            Date sqlDate;
            try {
                String rawDate = req.getPreferredDate();
                if (rawDate != null && rawDate.contains("-")) {
                    String[] parts = rawDate.split("-");
                    if (parts[0].length() == 4) {
                        sqlDate = Date.valueOf(rawDate.trim());
                    } else if (parts.length == 3 && parts[2].length() == 4) {
                        sqlDate = Date.valueOf(parts[2].trim() + "-" + parts[1].trim() + "-" + parts[0].trim());
                    } else {
                        sqlDate = Date.valueOf(LocalDate.now());
                    }
                } else {
                    sqlDate = Date.valueOf(LocalDate.now());
                }
            } catch (Exception ex) {
                sqlDate = Date.valueOf(LocalDate.now());
            }
            stmt.setDate(7, sqlDate);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("--- ACTUAL INSERT FAILURE CAUSE ---");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<RepairRequest> getRequestsByUserId(int userId) {
        List<RepairRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM repair_requests WHERE user_id = ? ORDER BY request_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RepairRequest req = new RepairRequest();
                req.setRequestId(rs.getString("request_id"));
                req.setUserId(rs.getInt("user_id"));
                req.setTechnicianId((Integer) rs.getObject("technician_id"));
                req.setServiceType(rs.getString("service_type"));
                req.setDeviceName(rs.getString("device_name"));
                req.setProblem(rs.getString("problem"));
                req.setDescription(rs.getString("description"));
                req.setPreferredDate(rs.getString("preferred_date"));
                req.setStatus(rs.getString("status"));
                list.add(req);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RepairRequest> getAllRequests() {
        List<RepairRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM repair_requests ORDER BY request_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RepairRequest req = new RepairRequest();
                req.setRequestId(rs.getString("request_id"));
                req.setUserId(rs.getInt("user_id"));
                req.setTechnicianId((Integer) rs.getObject("technician_id"));
                req.setServiceType(rs.getString("service_type"));
                req.setDeviceName(rs.getString("device_name"));
                req.setProblem(rs.getString("problem"));
                req.setDescription(rs.getString("description"));
                req.setPreferredDate(rs.getString("preferred_date"));
                req.setStatus(rs.getString("status"));
                list.add(req);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean assignTechnician(String requestId, int technicianId) {
        String sql = "UPDATE repair_requests SET technician_id = ?, status = 'Assigned' WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, technicianId);
            stmt.setString(2, requestId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String requestId, String status) {
        String sql = "UPDATE repair_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, requestId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteRequest(String requestId) {
        String sql = "DELETE FROM repair_requests WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
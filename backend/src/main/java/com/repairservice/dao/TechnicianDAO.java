package com.repairservice.dao;

import com.repairservice.model.Technician;
import com.repairservice.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {
    public boolean addTechnician(Technician tech) {
        String sql = "INSERT INTO technicians (name, phone, specialization) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tech.getName());
            stmt.setString(2, tech.getPhone());
            stmt.setString(3, tech.getSpecialization());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Technician> getAllTechnicians() {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM technicians";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Technician tech = new Technician();
                tech.setTechnicianId(rs.getInt("technician_id"));
                tech.setName(rs.getString("name"));
                tech.setPhone(rs.getString("phone"));
                tech.setSpecialization(rs.getString("specialization"));
                list.add(tech);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteTechnician(int techId) {
        String sql = "DELETE FROM technicians WHERE technician_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, techId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
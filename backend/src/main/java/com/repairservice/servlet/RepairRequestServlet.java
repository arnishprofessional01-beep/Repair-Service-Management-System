package com.repairservice.servlet;

import com.repairservice.dao.RepairRequestDAO;
import com.repairservice.model.RepairRequest;
import com.repairservice.model.User;
import com.repairservice.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@WebServlet("/api/requests")
public class RepairRequestServlet extends HttpServlet {
    private RepairRequestDAO requestDAO = new RepairRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String userEmail = request.getParameter("email");
        
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        int userId = -1;
        String userRole = "";

        if (user != null) {
            userId = user.getUserId();
            userRole = user.getRole();
        } else if (userEmail != null && !userEmail.trim().isEmpty()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT user_id, role FROM users WHERE email = ?")) {
                ps.setString(1, userEmail.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    userRole = rs.getString("role");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<RepairRequest> list;
        if ("Admin".equalsIgnoreCase(userRole)) {
            list = requestDAO.getAllRequests();
        } else if (userId != -1) {
            list = requestDAO.getRequestsByUserId(userId);
        } else {
            list = requestDAO.getAllRequests();
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            RepairRequest r = list.get(i);
            json.append("{")
                .append("\"requestId\":\"").append(r.getRequestId() != null ? r.getRequestId() : "").append("\",")
                .append("\"serviceType\":\"").append(r.getServiceType() != null ? r.getServiceType() : "").append("\",")
                .append("\"deviceName\":\"").append(r.getDeviceName() != null ? r.getDeviceName() : "").append("\",")
                .append("\"problem\":\"").append(r.getProblem() != null ? r.getProblem() : "").append("\",")
                .append("\"status\":\"").append(r.getStatus() != null ? r.getStatus() : "Pending").append("\",")
                .append("\"preferredDate\":\"").append(r.getPreferredDate() != null ? r.getPreferredDate() : "").append("\"")
                .append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");

        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String customerEmail = request.getParameter("customerEmail");
        int userId = -1;

        if (user != null) {
            userId = user.getUserId();
        } else if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE email = ?")) {
                ps.setString(1, customerEmail.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Ultimate safety fallback: use first valid user if session/email lookup was blank
        if (userId <= 0) {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id FROM users ORDER BY user_id LIMIT 1")) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String serviceType = request.getParameter("serviceType");
        String deviceName = request.getParameter("deviceName");
        String problem = request.getParameter("problem");
        String description = request.getParameter("description");
        String preferredDate = request.getParameter("preferredDate");

        String generatedId = requestDAO.generateNextRequestId();

        RepairRequest req = new RepairRequest();
        req.setRequestId(generatedId);
        req.setUserId(userId > 0 ? userId : 1);
        req.setServiceType(serviceType != null && !serviceType.trim().isEmpty() ? serviceType : "Laptop Repair");
        req.setDeviceName(deviceName != null && !deviceName.trim().isEmpty() ? deviceName : "Standard Device");
        req.setProblem(problem != null && !problem.trim().isEmpty() ? problem : "General Issue");
        req.setDescription(description != null ? description : "");
        req.setPreferredDate(preferredDate);

        boolean success = requestDAO.createRequest(req);

        if (success) {
            response.getWriter().write("{\"success\": true, \"requestId\": \"" + generatedId + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Database insert failed. Check Tomcat log.\"}");
        }
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestId = request.getParameter("requestId");
        boolean success = false;
        if (requestId != null && !requestId.trim().isEmpty()) {
            success = requestDAO.deleteRequest(requestId.trim());
        }
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }
}
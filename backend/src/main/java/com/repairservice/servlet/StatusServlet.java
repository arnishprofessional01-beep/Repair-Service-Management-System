package com.repairservice.servlet;

import com.repairservice.dao.RepairRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/status")
public class StatusServlet extends HttpServlet {
    private RepairRequestDAO requestDAO = new RepairRequestDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestId = request.getParameter("requestId");
        String status = request.getParameter("status");
        String techIdStr = request.getParameter("technicianId");

        boolean success = false;
        if (techIdStr != null && !techIdStr.trim().isEmpty()) {
            int techId = Integer.parseInt(techIdStr);
            success = requestDAO.assignTechnician(requestId, techId);
        } else if (status != null && !status.trim().isEmpty()) {
            success = requestDAO.updateStatus(requestId, status);
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }
}
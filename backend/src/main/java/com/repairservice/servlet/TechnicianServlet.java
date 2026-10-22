package com.repairservice.servlet;

import com.repairservice.dao.TechnicianDAO;
import com.repairservice.model.Technician;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/technicians")
public class TechnicianServlet extends HttpServlet {
    private TechnicianDAO techDAO = new TechnicianDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Technician> list = techDAO.getAllTechnicians();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Technician t = list.get(i);
            json.append("{")
                .append("\"technicianId\":").append(t.getTechnicianId()).append(",")
                .append("\"name\":\"").append(t.getName()).append("\",")
                .append("\"phone\":\"").append(t.getPhone()).append("\",")
                .append("\"specialization\":\"").append(t.getSpecialization()).append("\"")
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
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String specialization = request.getParameter("specialization");

        Technician tech = new Technician(name, phone, specialization);
        boolean success = techDAO.addTechnician(tech);

        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String techIdStr = request.getParameter("technicianId");
        boolean success = false;
        if (techIdStr != null && !techIdStr.trim().isEmpty()) {
            try {
                int techId = Integer.parseInt(techIdStr.trim());
                success = techDAO.deleteTechnician(techId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }
}
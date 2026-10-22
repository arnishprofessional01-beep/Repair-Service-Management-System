package com.repairservice.servlet;

import com.repairservice.dao.UserDAO;
import com.repairservice.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        // Read selected role from form; default to "Customer" if missing
        String roleParam = request.getParameter("role");
        String role = "Customer";
        if (roleParam != null && !roleParam.trim().isEmpty()) {
            role = roleParam.substring(0, 1).toUpperCase() + roleParam.substring(1).toLowerCase();
        }

        User user = new User(name, email, phone, password, role);
        boolean isSuccess = userDAO.registerUser(user);

        response.setContentType("application/json");
        if (isSuccess) {
            response.getWriter().write("{\"success\": true, \"message\": \"Registration successful! Please login.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"Registration failed. Email might already exist.\"}");
        }
    }
}
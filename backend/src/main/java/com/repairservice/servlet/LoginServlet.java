package com.repairservice.servlet;

import com.repairservice.dao.UserDAO;
import com.repairservice.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = userDAO.authenticateUser(email, password);
        response.setContentType("application/json");

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            String redirectUrl = "Customer".equalsIgnoreCase(user.getRole()) 
                ? "customer/dashboard.html" 
                : "admin/dashboard.html";

            response.getWriter().write("{\"success\": true, \"redirect\": \"" + redirectUrl + "\", \"role\": \"" + user.getRole() + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid email or password.\"}");
        }
    }
}
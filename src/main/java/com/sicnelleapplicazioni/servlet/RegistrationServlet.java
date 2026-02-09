package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.RegistrationService;
import com.sicnelleapplicazioni.util.ValidationUtil;

import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    private RegistrationService registrationService;

    @Override
    public void init() throws ServletException {
        // Instantiate dependencies
        UserRepository userRepository = new JdbcUserRepository();
        this.registrationService = new RegistrationService(userRepository); // Updated constructor
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        char[] password = req.getParameter("password").toCharArray();
        String fullName = req.getParameter("fullName"); // New parameter

        // Server-side Validation
        if (!ValidationUtil.isValidUsername(username)) {
            session.setAttribute("errorMessage", "Invalid username. Username must be alphanumeric and between 3 and 20 characters.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            session.setAttribute("errorMessage", "Invalid email address. Please enter a valid email.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }
        // Basic full name validation (can be expanded)
        if (fullName == null || fullName.trim().isEmpty() || fullName.length() > 100) {
            session.setAttribute("errorMessage", "Full name is required and cannot exceed 100 characters.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        boolean success = registrationService.register(username, email, password, fullName);

        // Add a random delay to mitigate timing attacks
        try {
            Thread.sleep(new SecureRandom().nextInt(500) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (success) {
            session.setAttribute("successMessage", "Registration successful! You can now log in."); // Removed email verification message
            resp.sendRedirect(req.getContextPath() + "/login.jsp"); // Redirect to login page
        } else {
            session.setAttribute("errorMessage", "Registration failed. Username or email might already be in use. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        }
    }
}
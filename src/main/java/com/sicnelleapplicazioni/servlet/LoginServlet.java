package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.User; // Import User model
import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.LoginService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional; // Import Optional

//@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private LoginService loginService;
    private UserRepository userRepository; // Add UserRepository to fetch user details after authentication

    @Override
    public void init() throws ServletException {
        // Instantiate dependencies
        userRepository = new JdbcUserRepository(); // Initialize userRepository
        this.loginService = new LoginService(userRepository); // Pass userRepository to LoginService
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String identifier = req.getParameter("email"); // Assuming login.jsp sends email as identifier
        char[] password = req.getParameter("password").toCharArray();

        if (loginService.authenticate(identifier, password)) {
            // Session Fixation mitigation: Change session ID after successful authentication
            if (req.getServletContext().getMajorVersion() >= 3 && req.getServletContext().getMinorVersion() >= 1) {
                req.changeSessionId(); // Servlet 3.1+
            } else {
                // For older Servlet versions, invalidate and create new (less ideal)
                // session.invalidate();
                // session = req.getSession(true);
            }

            // Fetch user to store correct userId and email in session
            Optional<User> userOptional = userRepository.findByEmail(identifier);
            if (userOptional.isEmpty()) {
                userOptional = userRepository.findByUsername(identifier); // Fallback to username if email not found
            }

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                session.setAttribute("userId", user.getId()); // Store userId
                session.setAttribute("email", user.getEmail()); // Store email
                session.setAttribute("username", user.getUsername()); // Store username (for display/legacy if needed)
                session.setAttribute("successMessage", "Login successful!");
                resp.sendRedirect(req.getContextPath() + "/home"); // Redirect to a home page
            } else {
                // This case should ideally not be reached if authentication was successful
                session.setAttribute("errorMessage", "An unexpected error occurred during login. Please try again.");
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
        } else {
            session.setAttribute("errorMessage", "Credenziali non valide. Riprova."); // Generic error message
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}

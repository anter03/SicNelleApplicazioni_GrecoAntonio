package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.service.LoginService;

import java.util.Optional;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Note: This code assumes the javax.servlet API is provided by the application server (e.g., Tomcat).
// No explicit dependency is included in the project for it.

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private LoginService loginService;

    @Override
    public void init() throws ServletException {
        // In a real application, the LoginService would be injected by a dependency injection framework.
        // For simplicity, we are not setting it up here.
        // this.loginService = ...
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        char[] password = req.getParameter("password").toCharArray(); // This is not ideal, see note below

        // Note: Retrieving password directly from request parameters as a String is not fully secure.
        // A more secure approach would involve a custom request wrapper or a different way to handle credentials.
        // However, for a standard HttpServletRequest, this is a common approach.

        Optional<User> user = loginService.login(username, password);

        // Add a random delay to mitigate timing attacks
        try {
            Thread.sleep(new SecureRandom().nextInt(500) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (user.isPresent()) {
            // Regenerate session ID to prevent session fixation
            req.changeSessionId();
            resp.sendRedirect(req.getContextPath() + "/home");
        } else {
            // Generic error message to prevent user enumeration
            resp.sendRedirect(req.getContextPath() + "/login?error=true");
        }
    }
}

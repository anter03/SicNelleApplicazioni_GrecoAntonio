package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.LoginService;
import com.sicnelleapplicazioni.security.PasswordUtil; // Assuming this exists for password verification

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private LoginService loginService;

    @Override
    public void init() throws ServletException {
        // Instantiate dependencies
        UserRepository userRepository = new JdbcUserRepository();
        this.loginService = new LoginService(userRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String username = req.getParameter("username");
        char[] password = req.getParameter("password").toCharArray();

        if (loginService.authenticate(username, password)) {
            session.setAttribute("username", username); // Store username in session
            session.setAttribute("successMessage", "Login successful!");
            resp.sendRedirect(req.getContextPath() + "/home.jsp"); // Redirect to a home page
        } else {
            session.setAttribute("errorMessage", "Invalid username or password.");
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}
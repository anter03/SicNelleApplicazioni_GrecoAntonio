package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.InMemoryUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/verify")
public class VerificationServlet extends HttpServlet {

    private UserRepository userRepository;

    @Override
    public void init() throws ServletException {
        // In a real application, the UserRepository would be injected.
        this.userRepository = new InMemoryUserRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String userIdParam = req.getParameter("id");
        String token = req.getParameter("token");

        if (userIdParam == null || userIdParam.isEmpty() || token == null || token.isEmpty()) {
            session.setAttribute("errorMessage", "Invalid verification link.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        Long userId = null;
        try {
            userId = Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid user ID in verification link.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
            return;
        }

        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Additional check for user ID consistency (optional but good practice)
            if (!user.getId().equals(userId)) {
                session.setAttribute("errorMessage", "Verification failed. User ID mismatch.");
                resp.sendRedirect(req.getContextPath() + "/register.jsp");
                return;
            }

            if (user.isEmailVerified()) {
                session.setAttribute("successMessage", "Your email is already verified.");
                resp.sendRedirect(req.getContextPath() + "/register.jsp");
                return;
            }

            user.setEmailVerified(true);
            user.setVerificationToken(null); // Clear token after successful verification
            userRepository.save(user); // Update user status

            session.setAttribute("successMessage", "Email verified successfully! You can now log in.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        } else {
            session.setAttribute("errorMessage", "Invalid or expired verification token.");
            resp.sendRedirect(req.getContextPath() + "/register.jsp");
        }
    }
}
package com.sicnelleapplicazioni.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp); // Process GET requests as POST for logout
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); 

        if (session != null) {
            session.invalidate(); // Invalidate the session
        }

        // Remove session cookies
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Invalidate JSESSIONID or any other session-related cookies
                if (cookie.getName().equalsIgnoreCase("JSESSIONID") || cookie.getName().equals("YOUR_APP_SESSION_COOKIE")) {
                    cookie.setMaxAge(0); // Set cookie to expire immediately
                    cookie.setPath(req.getContextPath() + "/"); // Set path to root of application
                    cookie.setHttpOnly(true); // Ensure HttpOnly flag
                    cookie.setSecure(req.isSecure()); // Ensure Secure flag if HTTPS
                    resp.addCookie(cookie);
                }
            }
        }

        // Set no-cache headers
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        resp.setHeader("Pragma", "no-cache");    // HTTP 1.0.
        resp.setHeader("Expires", "0");          // Proxies.

        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
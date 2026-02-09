package com.sicnelleapplicazioni.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // Protects all URLs by default
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // Do not create a new session if one doesn't exist

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Define unprotected paths
        boolean isLoginRequest = path.startsWith("/login");
        boolean isRegisterRequest = path.startsWith("/register");
        boolean isCssResource = path.startsWith("/css/");
        boolean isLoginPage = path.equals("/login.jsp");
        boolean isRegisterPage = path.equals("/register.jsp");

        // Check if the user is authenticated
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        if (isAuthenticated || isLoginRequest || isRegisterRequest || isCssResource || isLoginPage || isRegisterPage) {
            // If authenticated or accessing an unprotected resource, continue the chain
            chain.doFilter(request, response);
        } else {
            // If not authenticated and trying to access a protected resource, redirect to login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
package com.sicnelleapplicazioni.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Debug dettagliato
        System.out.println("Filter - Path: " + path);
        System.out.println("Filter - Method: " + httpRequest.getMethod());
        
        // Risorse pubbliche (non richiedono autenticazione)
        if (isPublicResource(path)) {
            System.out.println("Public resource - allowing");
            chain.doFilter(request, response);
            return;
        }
        
        // Verifica autenticazione
        HttpSession session = httpRequest.getSession(false);
        
        

        

        
        // 🔍 AGGIUNGI QUESTO DEBUG
        System.out.println("=== AuthenticationFilter ===");
        System.out.println("Full URI: " + httpRequest.getRequestURI());
        System.out.println("Context Path: " + httpRequest.getContextPath());
        System.out.println("Computed Path: " + path);
        System.out.println("Session exists: " + (session != null));
        System.out.println("userId in session: " + (session != null ? session.getAttribute("userId") : "NO SESSION"));
        System.out.println("============================");
        
        
        
        
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);
        
        System.out.println("Session: " + (session != null ? session.getId() : "null"));
        System.out.println("Authenticated: " + isAuthenticated);
        
        if (isAuthenticated) {
            chain.doFilter(request, response);
        } else {
            System.out.println("Redirecting to login");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }
    
    private boolean isPublicResource(String path) {
        return path.equals("/login.jsp") ||
               path.equals("/register.jsp") ||
               path.equals("/login") ||  // servlet path
               path.equals("/register") || // servlet path
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/");
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void destroy() {}
}
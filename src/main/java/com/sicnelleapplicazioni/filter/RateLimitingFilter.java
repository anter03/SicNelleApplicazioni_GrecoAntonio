package com.sicnelleapplicazioni.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@WebFilter("/login")
public class RateLimitingFilter implements Filter {

    // Usciamo dalla logica della Map infinita.
    // Usiamo una Cache che "dimentica" gli IP dopo 15 minuti di inattività.
    // Questo protegge il server dal riempimento della RAM (DoS attack).
    private final Cache<String, RateLimiter> ipRateLimiters = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10000) // Protezione massima: non memorizza più di 10k IP contemporaneamente
            .build();

    private final Cache<String, RateLimiter> usernameRateLimiters = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ipAddress = httpRequest.getRemoteAddr();
        String username = httpRequest.getParameter("username");

        try {
            // 1. CONTROLLO IP
            // RateLimiter.create(2.0) = 2 permessi al secondo.
            // Permette un uso fluido ma blocca script impazziti.
            RateLimiter forIp = ipRateLimiters.get(ipAddress, () -> RateLimiter.create(2.0));
            
            if (!forIp.tryAcquire()) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.getWriter().write("Too many requests. Slow down.");
                return;
            }

            // 2. CONTROLLO USERNAME (solo se presente)
            if (username != null && !username.trim().isEmpty()) {
                // RateLimiter.create(0.5) = 1 permesso ogni 2 secondi.
                RateLimiter forUser = usernameRateLimiters.get(username, () -> RateLimiter.create(0.5));
                
                if (!forUser.tryAcquire()) {
                    httpResponse.setStatus(429);
                    httpResponse.getWriter().write("Too many login attempts. Please wait a moment.");
                    return;
                }
            }

        } catch (ExecutionException e) {
            // Gestione errore nel caso improbabile che la cache fallisca la creazione
            throw new ServletException("Rate limiter error", e);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inizializzazione se necessaria
    }

    @Override
    public void destroy() {
        // Pulizia se necessaria
    }
}
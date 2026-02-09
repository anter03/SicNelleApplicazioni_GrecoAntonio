package com.sicnelleapplicazioni.filter;

// Note: This filter uses the Guava library's RateLimiter.
// The Guava library must be included as a dependency in the project.
import com.google.common.util.concurrent.RateLimiter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebFilter("/login")
public class RateLimitingFilter implements Filter {

    private final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> usernameRateLimiters = new ConcurrentHashMap<>();
    private final RateLimiter ipRateLimiter = RateLimiter.create(10.0 / 60.0); // 10 requests per minute
    private final RateLimiter usernameRateLimiter = RateLimiter.create(3.0 / 60.0); // 3 requests per minute

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ipAddress = httpRequest.getRemoteAddr();
        String username = httpRequest.getParameter("username");

        RateLimiter forIp = ipRateLimiters.computeIfAbsent(ipAddress, k -> RateLimiter.create(10.0 / 60.0));
        if (!forIp.tryAcquire()) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Too many requests from your IP address. Please try again later.");
            return;
        }

        if (username != null) {
            RateLimiter forUser = usernameRateLimiters.computeIfAbsent(username, k -> RateLimiter.create(3.0 / 60.0));
            if (!forUser.tryAcquire()) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Too many login attempts for this username. Please try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

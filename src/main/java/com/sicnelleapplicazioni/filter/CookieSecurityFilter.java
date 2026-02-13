package com.sicnelleapplicazioni.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class CookieSecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Lasciamo che la richiesta prosegua verso la Servlet (Login o Home)
        // È qui che il server crea la sessione e genera l'header Set-Cookie
        chain.doFilter(request, response);

        // 2. intercetto  header prima  del browser
        Collection<String> headers = httpResponse.getHeaders("Set-Cookie");

        if (headers != null && !headers.isEmpty()) {
            boolean first = true;
            for (String header : headers) {
                // Verifichiamo se è il cookie di sessione e se non ha già il SameSite
                if (header.contains("JSESSIONID") && !header.contains("SameSite")) {
                    String secureHeader = header + "; SameSite=Strict";

                    if (first) {
                        httpResponse.setHeader("Set-Cookie", secureHeader);
                        first = false;
                    } else {
                        httpResponse.addHeader("Set-Cookie", secureHeader);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {}
}
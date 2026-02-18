package com.sicnelleapplicazioni.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro di controllo sessione e autenticazione.
 * Implementa:
 * - Controllo autenticazione su tutte le pagine protette
 * - Protezione contro accesso non autorizzato
 * - Gestione redirect dopo login
 */
@WebFilter("/*")
public class SessionControlFilter implements Filter {

    // Percorsi pubblici (non richiedono autenticazione)
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/login",
            "/register",
            "/login.jsp",
            "/register.jsp",
            "/error404.jsp",
            "/error500.jsp"
    ));

    // Prefissi di risorse statiche
    private static final Set<String> STATIC_PREFIXES = new HashSet<>(Arrays.asList(
            "/css/",
            "/js/",
            "/images/",
            "/fonts/",
            "/assets/"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[SECURITY] SessionControlFilter initialized at " +
                new java.util.Date());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;



        // Impedisce che il sito venga caricato in un <iframe> (Protezione Clickjacking - RF 3.7)
        httpResponse.setHeader("X-Frame-Options", "DENY");
        // Blocca il caricamento di script se il browser rileva XSS (Protezione XSS - RF 3.4)
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        // Impedisce al browser di interpretare i file come MIME-type diversi da quelli dichiarati
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");


        // Calcola il path relativo
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // Normalizza il path (gestisce /login vs /login/)
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        // 1. VERIFICA SE È UNA RISORSA PUBBLICA
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. CONTROLLO SESSIONE E AUTENTICAZIONE
        String msg = "Sessione scaduta o non valida";
        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null &&
                session.getAttribute("userId") != null &&
                Boolean.TRUE.equals(session.getAttribute("authenticated")));

        if (isAuthenticated) {
            // 3. VALIDAZIONE AGGIUNTIVA SESSIONE

            // Controllo timeout sessione
            try {
                session.getCreationTime(); // Lancia eccezione se sessione invalidata
            } catch (IllegalStateException e) {
                System.out.println("[SECURITY] Invalid session detected");
                redirectToLogin(httpRequest, httpResponse, path,msg);
                return;
            }

            // 4. UTENTE AUTENTICATO - Procedi
            chain.doFilter(request, response);
        } else {
            // 5. UTENTE NON AUTENTICATO - Redirect a login

            System.out.println("[SECURITY] Unauthorized access attempt to: " + path);
            redirectToLogin(httpRequest, httpResponse, path,msg);
        }
    }

    /**
     * Verifica se il path è una risorsa pubblica
     */
    private boolean isPublicResource(String path) {
        // Exact match
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        // Prefix match per risorse statiche
        for (String prefix : STATIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Redirect a login salvando l'URL originale
     */
    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String originalPath,
                                 String msg ) throws IOException {

        // Salva l'URL originale per redirect post-login (solo per GET)
        if ("GET".equalsIgnoreCase(request.getMethod()) &&
                !originalPath.equals("/") &&
                !originalPath.isEmpty()) {

            HttpSession session = request.getSession(true);
            session.setAttribute("errorMessage", msg);
            session.setAttribute("redirectAfterLogin",
                    request.getContextPath() + originalPath);
        }

        // Redirect a login
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @Override
    public void destroy() {
        System.out.println("[SECURITY] SessionControlFilter destroyed at " +
                new java.util.Date());
    }
}
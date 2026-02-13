package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.LoginService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class LoginServlet extends HttpServlet {

    private LoginService loginService;
    private UserRepository userRepository;

    @Override
    public void init() throws ServletException {
        userRepository = new JdbcUserRepository();
        this.loginService = new LoginService(userRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Se l'utente è già autenticato, redirect alla home
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. VALIDAZIONE INPUT
        String identifier = req.getParameter("email");
        String passwordRaw = req.getParameter("password");

        // Validazione base
        if (identifier == null || identifier.trim().isEmpty() ||
                passwordRaw == null || passwordRaw.isEmpty()) {
            req.setAttribute("errorMessage", "Tutti i campi sono obbligatori.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        char[] password = passwordRaw.toCharArray();

        try {
            // 2. AUTENTICAZIONE
            if (loginService.authenticate(identifier.trim(), password)) {

                // 3. MITIGAZIONE SESSION FIXATION
                // Invalida qualsiasi sessione pre-esistente
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // 4. CREAZIONE NUOVA SESSIONE SICURA
                HttpSession session = req.getSession(true);

                // Cambia l'ID della sessione (protezione Session Fixation)
                req.changeSessionId();

                // 5. RECUPERO UTENTE E POPOLAMENTO SESSIONE
                Optional<User> userOptional = userRepository.findByEmail(identifier.trim());
                if (userOptional.isEmpty()) {
                    userOptional = userRepository.findByUsername(identifier.trim());
                }

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // 6. SALVATAGGIO DATI IN SESSIONE (solo dati essenziali)
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("email", user.getEmail());
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("authenticated", Boolean.TRUE);

                    // 7. TIMEOUT SESSIONE (30 minuti)
                    session.setMaxInactiveInterval(1800);

                    // 8. LOGGING SICURO (non loggare password!)
                    System.out.println("[SECURITY] Login successful for user: " + user.getEmail() +
                            " | Session ID: " + session.getId());

                    // 9. REDIRECT POST-LOGIN
                    String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                    session.removeAttribute("redirectAfterLogin");

                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        resp.sendRedirect(redirectUrl);
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/home");
                    }
                } else {
                    // Caso teoricamente impossibile
                    throw new ServletException("User not found after successful authentication");
                }
            } else {
                // 10. GESTIONE ERRORE - Messaggio generico (anti enumeration attack)
                req.setAttribute("errorMessage", "Credenziali non valide. Riprova.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            }
        } finally {
            // 11. PULIZIA SICURA DELLA PASSWORD IN MEMORIA
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public void destroy() {
        // Cleanup resources if needed
    }
}
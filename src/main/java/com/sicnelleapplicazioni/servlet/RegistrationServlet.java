package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.repository.JdbcUserRepository;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.service.RegistrationService;
import com.sicnelleapplicazioni.util.ValidationUtil;

import java.security.SecureRandom;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class RegistrationServlet extends HttpServlet {

    private RegistrationService registrationService;

    @Override
    public void init() throws ServletException {
        UserRepository userRepository = new JdbcUserRepository();
        this.registrationService = new RegistrationService(userRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Accesso corretto alla JSP protetta
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String rawPassword = req.getParameter("password");
        String fullName = req.getParameter("fullName");

        // 1. Validazione Robustezza Password (RF1)
        if (!ValidationUtil.isStrongPassword(rawPassword)) {
            req.setAttribute("errorMessage", "La password deve contenere almeno 8 caratteri, una maiuscola, un numero e un carattere speciale.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }



        if (username == null || email == null || rawPassword == null || fullName == null) {
            // USO REQUEST: il forward mantiene la stessa richiesta
            req.setAttribute("errorMessage", "Tutti i campi sono obbligatori.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        char[] password = rawPassword.toCharArray();
        try {
            if (!ValidationUtil.isValidUsername(username) || !ValidationUtil.isValidEmail(email)) {
                req.setAttribute("errorMessage", "Dati inseriti non validi.");
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
                return;
            }

            boolean success = registrationService.register(username, email, password, fullName);

            // Mitigazione Timing Attack (RF 3.7)
            Thread.sleep(new SecureRandom().nextInt(300) + 200);

            if (success) {
                // USO SESSION: il redirect crea una nuova richiesta, serve la sessione per "trasportare" il messaggio
                req.getSession().setAttribute("successMessage", "Registrazione completata! Puoi accedere.");
                resp.sendRedirect(req.getContextPath() + "/login");
            } else {
                req.setAttribute("errorMessage", "Account già esistente o errore di sistema.");
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Errore generico durante la registrazione.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } finally {
            Arrays.fill(password, '\0');
        }
    }


}
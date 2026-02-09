package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Import HttpSession
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/home") // ChangeWebServlet mapping to /home
public class DisplayContentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DisplayContentServlet.class.getName());
    private static final String UPLOAD_DIRECTORY = "/tmp/uploads"; // Define UPLOAD_DIRECTORY
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        this.contentRepository = new JdbcContentRepository();
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not ensure upload directory exists for reading content", e);
            throw new ServletException("Could not ensure upload directory exists for reading content", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Content> contents = Collections.emptyList();
        String errorMessage = null;
        HttpSession session = req.getSession(false); // Do not create session if it doesn't exist

        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            try {
                // Fetch content specific to the logged-in user
                contents = contentRepository.findByUserId(userId);

                // Read the actual content from files for display
                for (Content content : contents) {
                    Path filePath = Paths.get(UPLOAD_DIRECTORY, content.getStoredFilename());
                    if (Files.exists(filePath)) {
                        content.setContentText(Files.readString(filePath, StandardCharsets.UTF_8));
                    } else {
                        LOGGER.log(Level.WARNING, "Content file not found: " + filePath.toString());
                        content.setContentText("[Content not found]");
                    }
                }
            } catch (Exception e) {
                String ipAddress = req.getRemoteAddr();
                LOGGER.log(Level.SEVERE,
                           String.format("Error retrieving content for User ID: %s, IP: %s, Error: %s",
                                         userId.toString(), ipAddress, e.getMessage()), e);
                errorMessage = "Impossibile recuperare i contenuti. Riprova più tardi o contatta l'assistenza.";
            }
        } else {
            // If no userId in session, content list remains empty, or redirect to login handled by filter
            // This case should ideally be handled by AuthenticationFilter, but provides a fallback
            errorMessage = "Autenticazione richiesta per visualizzare i contenuti.";
        }

        req.setAttribute("contents", contents);
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
        }
        req.getRequestDispatcher("/home.jsp").forward(req, resp); // Forward to home.jsp
    }
}
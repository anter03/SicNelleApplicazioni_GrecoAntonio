package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/home")
public class DisplayContentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DisplayContentServlet.class.getName());
    // UPLOAD_DIRECTORY should be in configuration, but keeping it here for now
    // This is the base directory where files are stored
    private static final String BASE_FILE_STORAGE_PATH = "/tmp/uploads"; 
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        this.contentRepository = new JdbcContentRepository();
        try {
            // Ensure the base directory exists
            Files.createDirectories(Paths.get(BASE_FILE_STORAGE_PATH));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not ensure base file storage directory exists", e);
            throw new ServletException("Could not ensure base file storage directory exists", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Content> contents = Collections.emptyList();
        String errorMessage = null;
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            try {
                contents = contentRepository.findByUserId(userId);

                for (Content content : contents) {
                    // Use content.getFilePath() which is the absolute path where the file is stored
                    Path fullFilePath = Paths.get(content.getFilePath());
                    if (Files.exists(fullFilePath)) {
                        // Read content text for preview
                        content.setContentText(Files.readString(fullFilePath, StandardCharsets.UTF_8));
                    } else {
                        LOGGER.log(Level.WARNING, "Content file not found: " + fullFilePath.toString() + " for content ID: " + content.getId());
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
            errorMessage = "Autenticazione richiesta per visualizzare i contenuti.";
        }

        req.setAttribute("contents", contents);
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
        }
        req.getRequestDispatcher("/home.jsp").forward(req, resp);
    }
}

package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Import StandardCharsets
import java.nio.file.Files; // Import Files
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/displayContent")
public class DisplayContentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DisplayContentServlet.class.getName());
    private static final String UPLOAD_DIRECTORY = "/tmp/uploads"; // Define UPLOAD_DIRECTORY
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        // In a real application, the ContentRepository would be injected.
        this.contentRepository = new JdbcContentRepository();
        // Ensure the upload directory exists for reading
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not ensure upload directory exists for reading content", e);
            throw new ServletException("Could not ensure upload directory exists for reading content", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Content> contents = Collections.emptyList(); // Default to empty list
        String errorMessage = null;

        try {
            // TODO: Implement user authentication and retrieve content specific to the logged-in user.
            // For now, let's fetch all content or content by a dummy user ID.
            contents = contentRepository.findAll(); // Example: Fetch all content

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
            Long userId = (Long) req.getSession().getAttribute("userId"); // Attempt to get userId from session

            LOGGER.log(Level.SEVERE,
                       String.format("Error retrieving content. User ID: %s, IP: %s, Error: %s",
                                     (userId != null ? userId.toString() : "N/A"), ipAddress, e.getMessage()), e);
            errorMessage = "Impossibile recuperare i contenuti. Riprova più tardi o contatta l'assistenza.";
        }

        req.setAttribute("contents", contents);
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
        }
        req.getRequestDispatcher("/displayContent.jsp").forward(req, resp);
    }
}

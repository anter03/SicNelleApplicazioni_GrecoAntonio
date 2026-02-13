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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DeleteServlet.class.getName());
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        this.contentRepository = new JdbcContentRepository();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing content ID.");
            return;
        }

        UUID contentId;
        try {
            contentId = UUID.fromString(idParam);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid content ID format.");
            return;
        }

        Optional<Content> contentOpt = contentRepository.findById(contentId);

        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            Path filePath = Paths.get(content.getFilePath());

            try {
                // First, delete the file from the filesystem
                Files.deleteIfExists(filePath);

                // Then, delete the record from the database
                contentRepository.delete(contentId);

                // Redirect to the home page to show the updated list
                resp.sendRedirect(req.getContextPath() + "/WEB-INF/views/home.jsp");

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error deleting file for content ID: " + contentId, e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting file.");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Content not found.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // To prevent accidental deletion via a simple link click, we can show a confirmation page.
        // For this implementation, we will redirect to the home page with an error message.
        req.getSession().setAttribute("errorMessage", "Deletion must be performed via a POST request.");
        resp.sendRedirect(req.getContextPath() + "/WEB-INF/views/home.jsp");
    }
}

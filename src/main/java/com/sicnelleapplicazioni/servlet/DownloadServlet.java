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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DownloadServlet.class.getName());
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        this.contentRepository = new JdbcContentRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing content ID.");
            return;
        }

        Long currentUserId = (Long) req.getSession().getAttribute("userId");
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

            // VERIFICA DI AUTORIZZAZIONE puoi csaricare solo i tuoi file
            if (!content.getUserId().equals(currentUserId)) {
                LOGGER.warning("Tentativo di accesso non autorizzato al file " + contentId + " da parte dell'utente " + currentUserId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Non hai i permessi per scaricare questo file.");
                return;
            }

            Path filePath = Paths.get(content.getFilePath());

            if (Files.exists(filePath)) {
                resp.setContentType(content.getMimeType());
                resp.setContentLengthLong(content.getSize());
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + content.getOriginalName() + "\"");

                try (OutputStream out = resp.getOutputStream()) {
                    Files.copy(filePath, out);
                    out.flush();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error writing file to output stream for content ID: " + contentId, e);
                    // It's likely too late to send an error to the client if the response has been committed
                }
            } else {
                LOGGER.log(Level.WARNING, "File not found for content ID: " + contentId);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Content not found.");
        }
    }
}

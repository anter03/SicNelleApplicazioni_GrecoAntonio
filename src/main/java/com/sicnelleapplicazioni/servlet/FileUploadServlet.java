package com.sicnelleapplicazioni.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.concurrent.Callable;

import com.sicnelleapplicazioni.util.ConfigManager;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;


//@WebServlet(name = "FileUploadServlet", urlPatterns = "/upload")

public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());

    // Percorso finale dove vuoi i file (Desktop)
    private static final String TARGET_STORAGE_PATH = ConfigManager.getProperty("uploadFilePath");

    private Tika tika;
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        try {
            // 1. Creiamo la directory di destinazione finale (Desktop) se non esiste
            Path uploadPath = Paths.get(TARGET_STORAGE_PATH);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. FIX PER L'ERRORE SMART TOMCAT:
            // Recuperiamo la directory temporanea che Tomcat VORREBBE usare
            File tempDir = (File) getServletContext().getAttribute(ServletContext.TEMPDIR);
            // Se quella maledetta cartella non esiste, la creiamo noi a forza.
            if (tempDir != null && !tempDir.exists()) {
                boolean created = tempDir.mkdirs();
                if (created) {
                    LOGGER.info("Cartella temporanea di Tomcat creata manualmente: " + tempDir.getAbsolutePath());
                } else {
                    LOGGER.warning("Impossibile creare la cartella temporanea: " + tempDir.getAbsolutePath());
                }
            }

            tika = new Tika();
            contentRepository = new JdbcContentRepository();

        } catch (IOException e) {
            throw new ServletException("Errore critico durante l'inizializzazione della servlet", e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String originalFileName = null;
        try {
            Part filePart = req.getPart("file");

            if (filePart == null || filePart.getSubmittedFileName() == null || filePart.getSize() == 0) {
                showError(req, resp, "Nessun file selezionato o file vuoto.");
                return;
            }

            originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            if (!originalFileName.toLowerCase().endsWith(".txt")) {
                showError(req, resp, "Solo i file .txt sono permessi (controllo estensione).");
                return;
            }

            Long userId = (Long) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
                return;
            }

            // Create a temporary file
            File tempDir = (File) getServletContext().getAttribute(ServletContext.TEMPDIR);
            if (tempDir == null) {
                tempDir = Files.createTempDirectory("sna_uploads_temp").toFile(); // Fallback if servlet context tempdir not set
            }
            Path tempFilePath = Files.createTempFile(tempDir.toPath(), "upload_", ".tmp");

            // Copy stream to temporary file
            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Execute file processing synchronously within doPost
            String resultMessage = new UploadProcessor(
                    tempFilePath, originalFileName, userId, tika, contentRepository,
                    TARGET_STORAGE_PATH, getServletContext().getAttribute(ServletContext.TEMPDIR)
            ).call();

            if (resultMessage.contains("caricato con successo")) {
                req.setAttribute("successMessage", "file caricato correttamente");
            } else if (resultMessage.contains("Contenuto del file non valido")) {
                req.setAttribute("errorMessage", resultMessage); // Use the specific error message
            }
            else {
                req.setAttribute("errorMessage", "caricamento file fallito: " + resultMessage); // Generic failure message
            }

            req.getRequestDispatcher("/upload.jsp").forward(req, resp);

        } catch (Exception e) {
            handleError(req, resp, e);
        }
    }

    private class UploadProcessor implements Callable<String> {
        private final Path tempFilePath;
        private final String originalFileName;
        private final Long userId;
        private final Tika processorTika;
        private final ContentRepository processorContentRepository;
        private final String processorTargetStoragePath;
        private final Object servletContextTempDir; // To maintain consistent logging context

        public UploadProcessor(Path tempFilePath, String originalFileName, Long userId,
                               Tika tika, ContentRepository contentRepository, String targetStoragePath, Object servletContextTempDir) {
            this.tempFilePath = tempFilePath;
            this.originalFileName = originalFileName;
            this.userId = userId;
            this.processorTika = tika;
            this.processorContentRepository = contentRepository;
            this.processorTargetStoragePath = targetStoragePath;
            this.servletContextTempDir = servletContextTempDir;
        }

        @Override
        public String call() throws Exception {
            Path finalDestination = null;
            try {
                // Validate MIME Type REAL on the temporary file
                String detectedMimeType = processorTika.detect(tempFilePath.toFile());

                if (!MediaType.TEXT_PLAIN.toString().equals(detectedMimeType)) {
                    LOGGER.warning("Contenuto del file non valido per: " + originalFileName + ". Rilevato: " + detectedMimeType);
                    return "Contenuto del file non valido. Accettiamo solo puro testo (text/plain).";
                }

                // Generate unique internal name and define final destination
                String internalFileName = UUID.randomUUID().toString() + ".txt";
                finalDestination = Paths.get(processorTargetStoragePath, internalFileName);

                // Move temporary file to final destination
                Files.move(tempFilePath, finalDestination, StandardCopyOption.REPLACE_EXISTING);

                Content content = new Content();
                content.setUserId(userId);
                content.setOriginalName(originalFileName);
                content.setInternalName(internalFileName);
                content.setMimeType(detectedMimeType);
                content.setSize(Files.size(finalDestination)); // Get size from final file
                content.setFilePath(finalDestination.toString());

                processorContentRepository.save(content);

                LOGGER.info("File '" + originalFileName + "' caricato e processato con successo da thread separato.");
                return "File '" + originalFileName + "' caricato con successo!";

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Errore durante l'elaborazione del file in thread separato: " + originalFileName, e);
                return "Errore durante l'elaborazione del file: " + e.getMessage();
            } finally {
                // Ensure temporary file is always deleted after processing
                if (tempFilePath != null && Files.exists(tempFilePath)) {
                    try {
                        Files.delete(tempFilePath);
                        LOGGER.log(java.util.logging.Level.INFO, "Temporary file deleted: " + tempFilePath);
                    } catch (IOException ex) {
                        LOGGER.log(java.util.logging.Level.SEVERE, "Error deleting temporary file in finally block: " + tempFilePath, ex);
                    }
                }
            }
        }
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
        // For specific, safe-to-display errors, we show them.
        // For generic ones, we use a standard message.
        if (msg.contains("Contenuto del file non valido")) {
            req.setAttribute("errorMessage", "Contenuto del file non valido. Accettiamo solo puro testo (text/plain).");
        } else {
            req.setAttribute("errorMessage", "caricamento file fallito");
        }
        req.getRequestDispatcher("/upload.jsp").forward(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, Exception e) throws ServletException, IOException {
        // Log robusto per capire cosa succede
        String ipAddress = req.getRemoteAddr();
        Object userIdObj = req.getSession().getAttribute("userId");
        String userId = (userIdObj != null) ? userIdObj.toString() : "Anonymous";

        LOGGER.log(java.util.logging.Level.SEVERE, "Upload error - User: " + userId + ", IP: " + ipAddress, e);

        req.setAttribute("errorMessage", "caricamento file fallito");
        req.getRequestDispatcher("/upload.jsp").forward(req, resp);
    }
}
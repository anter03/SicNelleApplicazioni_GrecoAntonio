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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Callable;

import com.sicnelleapplicazioni.util.ConfigManager;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;


//@WebServlet(name = "FileUploadServlet", urlPatterns = "/upload")
@MultipartConfig // Necessario per gestire i file upload
public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());

    // Percorso finale dei file
    private static final String TARGET_STORAGE_PATH = ConfigManager.getProperty("uploadFilePath");

    // Risorsa condivisa: oggetto monitor per il lock (Per Test TU10)
    private static final Object logLock = new Object();

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

            // 2. FIX PER odioso errore SMART TOMCAT:
            File tempDir = (File) getServletContext().getAttribute(ServletContext.TEMPDIR);
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(req, resp);
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

            // --- SANITIZZAZIONE NOME FILE (Difesa contro Stored XSS e Path Traversal - RF 3.4 / TA5) ---
            originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

            // Limitiamo anche la lunghezza del nome file
            if (originalFileName.length() > 255) {
                originalFileName = originalFileName.substring(0, 250) + ".txt";
            }

            if (!originalFileName.toLowerCase().endsWith(".txt")) {
                showError(req, resp, "Estensione non valida");
                return;
            }

            Long userId = (Long) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.sendRedirect(req.getContextPath() + "/WEB-INF/views/login.jsp");
                return;
            }

            // Create a temporary file
            File tempDir = (File) getServletContext().getAttribute(ServletContext.TEMPDIR);
            if (tempDir == null) {
                tempDir = Files.createTempDirectory("sna_uploads_temp").toFile();
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
                req.setAttribute("errorMessage", resultMessage);
            }
            else {
                req.setAttribute("errorMessage", "caricamento file fallito: " + resultMessage);
            }

            req.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(req, resp);

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
        private final Object servletContextTempDir;

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
                // Validate MIME Type REAL
                String detectedMimeType = processorTika.detect(tempFilePath.toFile());

                if (!MediaType.TEXT_PLAIN.toString().equals(detectedMimeType)) {
                    LOGGER.warning("Contenuto del file non valido per: " + originalFileName + ". Rilevato: " + detectedMimeType);
                    return "Contenuto del file non valido. Accettiamo solo file  .txt.";
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

                // --- TU10 ---

                updateAuditLog(String.valueOf(userId), originalFileName);

                LOGGER.info("File '" + originalFileName + "' caricato e processato con successo da thread separato.");
                return "File '" + originalFileName + "' caricato con successo!";

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Errore durante l'elaborazione del file in thread separato: " + originalFileName, e);
                return "Errore durante l'elaborazione del file: " + e.getMessage();
            } finally {
                // Controllo che il file temporaneo venga cancelato
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
        if (msg != "") {
            req.setAttribute("errorMessage", msg);
        } else {
            req.setAttribute("errorMessage", "caricamento file fallito");
        }
        req.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, Exception e) throws ServletException, IOException {
        String ipAddress = req.getRemoteAddr();
        Object userIdObj = req.getSession().getAttribute("userId");
        String userId = (userIdObj != null) ? userIdObj.toString() : "Anonymous";
        LOGGER.log(java.util.logging.Level.SEVERE, "Upload error - User: " + userId + ", IP: " + ipAddress, e);
        req.setAttribute("errorMessage", "caricamento file fallito");
        req.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(req, resp);
    }

    // ---  (TU10) ---
    private void updateAuditLog(String username, String fileName) {
        Path logPath = Paths.get(TARGET_STORAGE_PATH, "audit_log.txt");

        // 1. Il blocco synchronized garantisce l'accesso esclusivo alla risorsa (TU10)
        synchronized(logLock) {
            try {
                String threadName = Thread.currentThread().getName();

                LOGGER.info("[" + threadName + "] Tenta di scrivere nel registro di audit...");

                String entry = String.format("[%s] USERID: %s | FILE: %s | THREAD: %s\n",
                        java.time.LocalDateTime.now(), username, fileName, threadName);


                Files.write(logPath,
                        entry.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        java.nio.file.StandardOpenOption.CREATE, // Crea il file se non esiste
                        java.nio.file.StandardOpenOption.APPEND); // Aggiunge in coda senza sovrascrivere



                LOGGER.info("[" + threadName + "] Scrittura completata e lock rilasciato.");

            } catch (IOException  e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Errore di concorrenza nel log di audit", e);
            }
        }
    }
}
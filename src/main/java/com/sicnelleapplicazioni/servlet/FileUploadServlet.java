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

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;


//@WebServlet(name = "FileUploadServlet", urlPatterns = "/upload")

public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());

    // Percorso finale dove vuoi i file (Desktop)
    private static final String TARGET_STORAGE_PATH = "C:\\Users\\anton\\Desktop\\tikaUploadSna";

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
        String message;
        Path tempFilePath = null; // Usato solo se decidiamo di salvare un temp file noi, ma qui andiamo diretti

        try {
            // Ora getPart non dovrebbe esplodere grazie al fix in init()
            Part filePart = req.getPart("file");

            if (filePart == null || filePart.getSubmittedFileName() == null || filePart.getSize() == 0) {
                showError(req, resp, "Nessun file selezionato o file vuoto.");
                return;
            }

            String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            // 1. Validazione estensione (veloce, ma non sicura)
            if (!originalFileName.toLowerCase().endsWith(".txt")) {
                showError(req, resp, "Solo i file .txt sono permessi (controllo estensione).");
                return;
            }

            // 2. Preparazione destinazione finale
            String internalFileName = UUID.randomUUID().toString() + ".txt";
            Path finalDestination = Paths.get(TARGET_STORAGE_PATH, internalFileName);

            // 3. Copia dello stream direttamente nella destinazione finale
            // Nota: Se il controllo Tika fallisce dopo, cancelleremo questo file.
            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, finalDestination, StandardCopyOption.REPLACE_EXISTING);
            }

            // 4. Validazione MIME Type REALE con Tika (sul file appena salvato)
            String detectedMimeType = tika.detect(finalDestination.toFile());

            if (!MediaType.TEXT_PLAIN.toString().equals(detectedMimeType)) {
                // Il file è bugiardo: ha estensione .txt ma dentro non lo è. Cancelliamo tutto.
                Files.deleteIfExists(finalDestination);
                showError(req, resp, "Contenuto del file non valido. Accettiamo solo puro testo (text/plain).");
                return;
            }

            // 5. Recupero utente
            Long userId = (Long) req.getSession().getAttribute("userId");
            if (userId == null) {
                Files.deleteIfExists(finalDestination); // Pulizia
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
                return;
            }

            // 6. Salvataggio su DB
            Content content = new Content();
            content.setUserId(userId);
            content.setOriginalName(originalFileName);
            content.setInternalName(internalFileName);
            content.setMimeType(detectedMimeType);
            content.setSize(Files.size(finalDestination));
            content.setFilePath(finalDestination.toString());

            contentRepository.save(content);

            // Successo
            message = "File '" + originalFileName + "' caricato con successo!";
            req.setAttribute("message", message);
            req.getRequestDispatcher("/upload.jsp").forward(req, resp);

        } catch (Exception e) {
            handleError(req, resp, e);
        }
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
        req.setAttribute("message", msg);
        req.getRequestDispatcher("/upload.jsp").forward(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, Exception e) throws ServletException, IOException {
        // Log robusto per capire cosa succede
        String ipAddress = req.getRemoteAddr();
        Object userIdObj = req.getSession().getAttribute("userId");
        String userId = (userIdObj != null) ? userIdObj.toString() : "Anonymous";

        LOGGER.log(java.util.logging.Level.SEVERE, "Upload error - User: " + userId + ", IP: " + ipAddress, e);

        req.setAttribute("message", "Errore critico nel caricamento del file: " + e.getMessage());
        req.getRequestDispatcher("/upload.jsp").forward(req, resp);
    }
}
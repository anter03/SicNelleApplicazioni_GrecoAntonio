package com.sicnelleapplicazioni.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import com.sicnelleapplicazioni.model.Content;
import com.sicnelleapplicazioni.repository.ContentRepository;
import com.sicnelleapplicazioni.repository.JdbcContentRepository;
import java.util.logging.Logger;
import java.util.UUID; // Import UUID
import java.util.Collections; // For potential empty list

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 5,      // 5 MB
    maxRequestSize = 1024 * 1024 * 10   // 10 MB
)
public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());
    private static final String BASE_FILE_STORAGE_PATH = "/tmp/uploads"; // Renamed and changed
    private Tika tika;
    private ContentRepository contentRepository;

    @Override
    public void init() throws ServletException {
        try {
            Files.createDirectories(Paths.get(BASE_FILE_STORAGE_PATH)); // Use BASE_FILE_STORAGE_PATH
            tika = new Tika();
            contentRepository = new JdbcContentRepository();
        } catch (IOException e) {
            throw new ServletException("Could not create upload directory", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = "";
        Path tempFile = null;
        try {
            Part filePart = req.getPart("file");
            String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            // Validate file extension
            if (!originalFileName.toLowerCase().endsWith(".txt")) {
                message = "Only .txt files are allowed for upload.";
                req.setAttribute("message", message);
                req.getRequestDispatcher("/upload.jsp").forward(req, resp);
                return;
            }

            tempFile = Files.createTempFile("upload-", ".tmp");
            long fileSize = Files.copy(filePart.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            String detectedMimeType = tika.detect(tempFile.toFile());

            if (!MediaType.TEXT_PLAIN.toString().equals(detectedMimeType)) {
                message = "Invalid file content type. Only plain text files are allowed.";
                req.setAttribute("message", message);
                req.getRequestDispatcher("/upload.jsp").forward(req, resp);
                return;
            }

            // Generate UUID filename (internalName)
            String internalFileName = UUID.randomUUID().toString() + ".txt";
            Path finalFilePath = Paths.get(BASE_FILE_STORAGE_PATH, internalFileName); // Use BASE_FILE_STORAGE_PATH
            Files.move(tempFile, finalFilePath, StandardCopyOption.REPLACE_EXISTING);
            tempFile = null;

            // Get userId from session
            Long userId = (Long) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
                return;
            }

            // Create Content object
            Content content = new Content();
            content.setUserId(userId);
            content.setOriginalName(originalFileName);
            content.setInternalName(internalFileName);
            content.setMimeType(detectedMimeType);
            content.setSize(fileSize);
            content.setFilePath(finalFilePath.toString()); // Store absolute path

            contentRepository.save(content);

            message = "File '" + originalFileName + "' uploaded successfully and content saved!";
            req.setAttribute("message", message);
            req.getRequestDispatcher("/upload.jsp").forward(req, resp);

        } catch (Exception e) {
            String ipAddress = req.getRemoteAddr();
            Long userId = (Long) req.getSession().getAttribute("userId");

            LOGGER.log(java.util.logging.Level.SEVERE,
                       String.format("File upload failed. User ID: %s, IP: %s, Error: %s",
                                     (userId != null ? userId.toString() : "N/A"), ipAddress, e.getMessage()), e);

            message = "Impossibile completare l'operazione di caricamento. Riprova più tardi o contatta l'assistenza.";
            req.setAttribute("message", message);
            req.getRequestDispatcher("/upload.jsp").forward(req, resp);
        } finally {
            if (tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.delete(tempFile);
                } catch (IOException e) {
                    System.err.println("Error deleting temporary file: " + e.getMessage());
                }
            }
        }
    }
}

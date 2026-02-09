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
import org.apache.tika.Tika; // Import Tika
import org.apache.tika.mime.MediaType; // Import MediaType
import java.nio.file.Path; // Import Path
import java.nio.charset.StandardCharsets; // Import StandardCharsets
import java.time.LocalDateTime; // Import LocalDateTime
import com.sicnelleapplicazioni.model.Content; // Import Content
import com.sicnelleapplicazioni.repository.ContentRepository; // Import ContentRepository
import com.sicnelleapplicazioni.repository.JdbcContentRepository; // Import JdbcContentRepository
import java.util.logging.Logger; // Import Logger

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 5,      // 5 MB
    maxRequestSize = 1024 * 1024 * 10   // 10 MB
)
public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName()); // Initialize Logger
    private static final String UPLOAD_DIRECTORY = "/tmp/uploads"; // Temporary directory for uploads
    private Tika tika; // Tika instance
    private ContentRepository contentRepository; // ContentRepository instance

    @Override
    public void init() throws ServletException {
        // Ensure the upload directory exists
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));
            tika = new Tika(); // Initialize Tika
            contentRepository = new JdbcContentRepository(); // Initialize ContentRepository
        } catch (IOException e) {
            throw new ServletException("Could not create upload directory", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = "";
        Path tempFile = null; // Declare tempFile here
        try {
            Part filePart = req.getPart("file"); // "file" is the name of the input field in the form
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.

            // Validate file extension
            if (!fileName.toLowerCase().endsWith(".txt")) {
                message = "Only .txt files are allowed for upload.";
                req.setAttribute("message", message);
                req.getRequestDispatcher("/upload.jsp").forward(req, resp);
                return;
            }

            // Create a temporary file to store the upload for Tika processing
            tempFile = Files.createTempFile("upload-", ".tmp");
            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Detect MIME type using Tika
            String detectedMediaType = tika.detect(tempFile.toFile());

            // Validate detected MIME type
            if (!MediaType.TEXT_PLAIN.toString().equals(detectedMediaType)) {
                message = "Invalid file content type. Only plain text files are allowed.";
                req.setAttribute("message", message);
                req.getRequestDispatcher("/upload.jsp").forward(req, resp);
                // tempFile will be deleted in finally block
                return;
            }

            // If validation passes, move the temporary file to the final upload directory
            // Files.move(tempFile, Paths.get(UPLOAD_DIRECTORY, fileName), StandardCopyOption.REPLACE_EXISTING); // No longer saving to file system directly after Tika

            String storedFilename = java.util.UUID.randomUUID().toString() + ".txt"; // Generate UUID filename
            Path finalFilePath = Paths.get(UPLOAD_DIRECTORY, storedFilename);
            Files.move(tempFile, finalFilePath, StandardCopyOption.REPLACE_EXISTING); // Save file to its final location
            tempFile = null; // Clear tempFile reference as it's been moved

            // TODO: Get actual user ID from session after authentication. For now, using a placeholder.
            Long userId = (Long) req.getSession().getAttribute("userId");
            if (userId == null) {
                // For demonstration, if no user ID, use a dummy one or redirect to login
                userId = 1L; // Dummy user ID
            }

            // Create and save Content object
            Content content = new Content();
            content.setUserId(userId);
            content.setFilename(fileName); // Original filename
            content.setStoredFilename(storedFilename); // UUID filename
            content.setUploadTime(LocalDateTime.now());
            contentRepository.save(content);

            message = "File '" + fileName + "' uploaded successfully and content saved!";
            req.setAttribute("message", message);
            req.getRequestDispatcher("/upload.jsp").forward(req, resp);

        } catch (Exception e) {
            String ipAddress = req.getRemoteAddr();
            Long userId = (Long) req.getSession().getAttribute("userId"); // Attempt to get userId from session

            // Log detailed error internally
            LOGGER.log(java.util.logging.Level.SEVERE,
                       String.format("File upload failed. User ID: %s, IP: %s, Error: %s",
                                     (userId != null ? userId.toString() : "N/A"), ipAddress, e.getMessage()), e);

            // Provide generic error message to user as per RF8
            message = "Impossibile completare l'operazione di caricamento. Riprova più tardi o contatta l'assistenza.";
            req.setAttribute("message", message);
            req.getRequestDispatcher("/upload.jsp").forward(req, resp);
        } finally {
            // Ensure temporary file is deleted even if an error occurs
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

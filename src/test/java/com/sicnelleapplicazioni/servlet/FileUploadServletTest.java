package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.repository.ContentRepository;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.fail;

public class FileUploadServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Part filePart;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private Tika tika; // Mock Tika
    @Mock
    private ContentRepository contentRepository; // Mock ContentRepository

    private FileUploadServlet fileUploadServlet;

    @BeforeEach
    void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        fileUploadServlet = new FileUploadServlet() {
            // Override init to inject mocks
            @Override
            public void init() throws ServletException {
                super.init();
                // Using reflection to set the private fields.
                try {
                    java.lang.reflect.Field tikaField = FileUploadServlet.class.getDeclaredField("tika");
                    tikaField.setAccessible(true);
                    tikaField.set(fileUploadServlet, tika);

                    java.lang.reflect.Field contentRepoField = FileUploadServlet.class.getDeclaredField("contentRepository");
                    contentRepoField.setAccessible(true);
                    contentRepoField.set(fileUploadServlet, contentRepository);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new ServletException(e);
                }
            }
        };
        fileUploadServlet.init();
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getSession()).thenReturn(mock(javax.servlet.http.HttpSession.class)); // Mock session
        // Ensure the UPLOAD_DIRECTORY exists for Tika.detect to not fail on file creation.
        try {
            Files.createDirectories(Paths.get("/tmp/uploads"));
        } catch (IOException e) {
            fail("Failed to create /tmp/uploads directory for testing: " + e.getMessage());
        }
    }

    @Test
    void testDoPost_SuccessfulTxtUpload() throws ServletException, IOException {
        String fileContent = "This is a test text file.";
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getSubmittedFileName()).thenReturn("test.txt");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        when(tika.detect(any(java.io.File.class))).thenReturn("text/plain");

        fileUploadServlet.doPost(request, response);

        verify(request, times(1)).setAttribute(eq("message"), contains("uploaded successfully"));
        verify(requestDispatcher, times(1)).forward(request, response);
        verify(contentRepository, times(1)).save(any());
    }

    @Test
    void testDoPost_InvalidExtension() throws ServletException, IOException {
        String fileContent = "This is a test image.";
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getSubmittedFileName()).thenReturn("test.jpg");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));

        fileUploadServlet.doPost(request, response);

        verify(request, times(1)).setAttribute(eq("message"), eq("Only .txt files are allowed for upload."));
        verify(requestDispatcher, times(1)).forward(request, response);
        verify(tika, never()).detect(any(java.io.File.class)); // Tika should not be called
        verify(contentRepository, never()).save(any());
    }

    @Test
    void testDoPost_InvalidMimeType() throws ServletException, IOException {
        String fileContent = "This is actually an image disguised as text.";
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getSubmittedFileName()).thenReturn("image.txt"); // Valid extension
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes()));
        when(tika.detect(any(java.io.File.class))).thenReturn("image/jpeg"); // Invalid MIME type

        fileUploadServlet.doPost(request, response);

        verify(request, times(1)).setAttribute(eq("message"), eq("Invalid file content type. Only plain text files are allowed."));
        verify(requestDispatcher, times(1)).forward(request, response);
        verify(tika, times(1)).detect(any(java.io.File.class));
        verify(contentRepository, never()).save(any());
    }
}

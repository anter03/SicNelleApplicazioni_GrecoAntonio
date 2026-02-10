package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileUploadServletTest {

    private FileUploadServlet fileUploadServlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Part filePart;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ServletConfig servletConfig;   // <--- AGGIUNTO QUESTO

    @Mock
    private ServletContext servletContext; // <--- AGGIUNTO QUESTO

    @BeforeEach
    void setUp() throws ServletException, NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        fileUploadServlet = new FileUploadServlet();

        // --- INIZIO FIX PER IL TUO ERRORE ---

        // 1. Configuriamo il mock del Context
        // Quando la servlet chiede la cartella temporanea, diamogli quella vera di sistema del PC
        File systemTempDir = new File(System.getProperty("java.io.tmpdir"));
        when(servletContext.getAttribute(ServletContext.TEMPDIR)).thenReturn(systemTempDir);

        // 2. Configuriamo il mock della Config
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        // 3. Inizializziamo la servlet PASSANDO la config mockata
        // Questo evita l'IllegalStateException
        fileUploadServlet.init(servletConfig);

        // --- FINE FIX ---

        // Iniezione del repository mockato (Reflection per campi privati)
        Field repositoryField = FileUploadServlet.class.getDeclaredField("contentRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(fileUploadServlet, contentRepository);
    }

    // ... Lascia qui sotto i tuoi @Test (testDoPost_SuccessfulTxtUpload, ecc.) ...
    // ... Assicurati solo che funzionino con i mock sopra ...
}
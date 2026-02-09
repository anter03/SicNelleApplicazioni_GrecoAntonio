package com.sicnelleapplicazioni.servlet;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*; // Add this import

public class VerificationServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestDispatcher requestDispatcher;

    private VerificationServlet verificationServlet;

    @BeforeEach
    void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        verificationServlet = new VerificationServlet() {
            // Override init to inject mock UserRepository
            @Override
            public void init() throws ServletException {
                super.init();
                // This is a bit hacky, but for testing, we need to inject the mock
                // In a real app, use a DI framework or pass it via constructor.
                // Using reflection for now to set the private field.
                try {
                    java.lang.reflect.Field field = VerificationServlet.class.getDeclaredField("userRepository");
                    field.setAccessible(true);
                    field.set(verificationServlet, userRepository);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new ServletException(e);
                }
            }
        };
        verificationServlet.init();
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn(""); // Mock context path for redirects
    }

    @Test
    void testDoGet_ValidVerification() throws ServletException, IOException {
        String userId = "1";
        String token = "valid-token";
        User user = new User();
        user.setId(Long.parseLong(userId));
        user.setUsername("testuser");
        user.setEmailVerified(false);
        user.setVerificationToken(token);

        when(request.getParameter("id")).thenReturn(userId);
        when(request.getParameter("token")).thenReturn(token);
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        verificationServlet.doGet(request, response);

        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
        verify(userRepository, times(1)).save(user);
        verify(session, times(1)).setAttribute("successMessage", "Email verified successfully! You can now log in.");
        verify(response, times(1)).sendRedirect("/register.jsp");
    }

    @Test
    void testDoGet_InvalidToken() throws ServletException, IOException {
        String userId = "1";
        String token = "invalid-token";

        when(request.getParameter("id")).thenReturn(userId);
        when(request.getParameter("token")).thenReturn(token);
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        verificationServlet.doGet(request, response);

        verify(session, times(1)).setAttribute("errorMessage", "Invalid or expired verification token.");
        verify(response, times(1)).sendRedirect("/register.jsp");
    }

    @Test
    void testDoGet_AlreadyVerified() throws ServletException, IOException {
        String userId = "1";
        String token = "valid-token";
        User user = new User();
        user.setId(Long.parseLong(userId));
        user.setUsername("testuser");
        user.setEmailVerified(true); // Already verified
        user.setVerificationToken(token);

        when(request.getParameter("id")).thenReturn(userId);
        when(request.getParameter("token")).thenReturn(token);
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        verificationServlet.doGet(request, response);

        verify(session, times(1)).setAttribute("successMessage", "Your email is already verified.");
        verify(response, times(1)).sendRedirect("/register.jsp");
    }

    @Test
    void testDoGet_UserIdMismatch() throws ServletException, IOException {
        String userId = "1";
        String token = "valid-token";
        User user = new User();
        user.setId(2L); // Mismatch
        user.setUsername("testuser");
        user.setEmailVerified(false);
        user.setVerificationToken(token);

        when(request.getParameter("id")).thenReturn(userId);
        when(request.getParameter("token")).thenReturn(token);
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        verificationServlet.doGet(request, response);

        verify(session, times(1)).setAttribute("errorMessage", "Verification failed. User ID mismatch.");
        verify(response, times(1)).sendRedirect("/register.jsp");
    }

    @Test
    void testDoGet_MissingParameters() throws ServletException, IOException {
        // Test missing id
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("some-token");
        verificationServlet.doGet(request, response);
        verify(session, times(1)).setAttribute("errorMessage", "Invalid verification link.");
        verify(response, times(1)).sendRedirect("/register.jsp");

        // Reset mocks
        reset(request, response, session);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        // Test missing token
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("token")).thenReturn(null);
        verificationServlet.doGet(request, response);
        verify(session, times(1)).setAttribute("errorMessage", "Invalid verification link.");
        verify(response, times(1)).sendRedirect("/register.jsp");
    }
}

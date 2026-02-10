package com.sicnelleapplicazioni.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class SessionControlFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private HttpSession mockSession;

    private SessionControlFilter sessionControlFilter;

    @BeforeEach
    void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        sessionControlFilter = new SessionControlFilter();
        // init is empty, no need to call it
    }

    @Test
    void testDoFilter_UserLoggedIn() throws IOException, ServletException {
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("userId")).thenReturn(123L); // Simulate logged-in user

        sessionControlFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    @Test
    void testDoFilter_UserNotLoggedIn_NoSession() throws IOException, ServletException {
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getContextPath()).thenReturn("/my-app");

        sessionControlFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verify(mockResponse, times(1)).sendRedirect("/my-app/login");
    }

    @Test
    void testDoFilter_UserNotLoggedIn_SessionExistsButNoUserAttribute() throws IOException, ServletException {
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("userId")).thenReturn(null); // Simulate session exists but no user
        when(mockRequest.getContextPath()).thenReturn("/my-app");

        sessionControlFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verify(mockResponse, times(1)).sendRedirect("/my-app/login");
    }
}

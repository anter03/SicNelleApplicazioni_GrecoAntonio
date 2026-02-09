package com.sicnelleapplicazioni.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class XFrameOptionsFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;

    private XFrameOptionsFilter xFrameOptionsFilter;

    @BeforeEach
    void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        xFrameOptionsFilter = new XFrameOptionsFilter();
        // init is empty, no need to call it
    }

    @Test
    void testDoFilter_SetsXFrameOptionsHeader() throws IOException, ServletException {
        xFrameOptionsFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockResponse, times(1)).setHeader("X-Frame-Options", "SAMEORIGIN");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
}

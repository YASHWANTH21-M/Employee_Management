package com.example.employeemanagement.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        userDetails = User.withUsername("yashwanth@gmail.com")
                .password("password")
                .roles("USER")
                .build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void filter_shouldContinueWhenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void filter_shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void filter_shouldAuthenticateWhenTokenIsValid()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("yashwanth@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "yashwanth@gmail.com"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "valid-token",
                userDetails
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractUsername("valid-token");

        verify(jwtService)
                .isTokenValid(
                        "valid-token",
                        userDetails
                );

        verify(userDetailsService)
                .loadUserByUsername(
                        "yashwanth@gmail.com"
                );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void filter_shouldNotAuthenticateWhenTokenIsInvalid()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("yashwanth@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "yashwanth@gmail.com"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "invalid-token",
                userDetails
        )).thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void filter_shouldContinueWhenJwtThrowsException()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer broken-token"
        );

        when(jwtService.extractUsername("broken-token"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void filter_shouldNotReplaceExistingAuthentication()
            throws ServletException, IOException {

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("yashwanth@gmail.com");

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractUsername("valid-token");

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void filter_shouldHandleLowerCaseBearer()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "bearer valid-token"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("yashwanth@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "yashwanth@gmail.com"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "valid-token",
                userDetails
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void filter_shouldRemoveAngleBracketsAroundToken()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer <valid-token>"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("yashwanth@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "yashwanth@gmail.com"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "valid-token",
                userDetails
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractUsername("valid-token");
    }
}
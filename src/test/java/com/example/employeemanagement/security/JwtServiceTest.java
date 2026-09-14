package com.example.employeemanagement.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Base64 encoded secret key
    private final String secretKey =
            "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JKV1Q=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                secretKey
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                3600000L
        );

        userDetails = User.withUsername("yashwanth@gmail.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generateToken_shouldGenerateValidToken() {

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateTokenWithClaims_shouldIncludeClaims() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");

        String token = jwtService.generateToken(
                claims,
                userDetails
        );

        assertNotNull(token);

        String username = jwtService.extractUsername(token);

        assertEquals(
                "yashwanth@gmail.com",
                username
        );
    }

    @Test
    void extractUsername_shouldReturnUsername() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(
                "yashwanth@gmail.com",
                username
        );
    }

    @Test
    void extractClaim_shouldReturnSubject() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractClaim(
                token,
                claims -> claims.getSubject()
        );

        assertEquals(
                "yashwanth@gmail.com",
                username
        );
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(userDetails);

        boolean result = jwtService.isTokenValid(
                token,
                userDetails
        );

        assertTrue(result);
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {

        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser =
                User.withUsername("another@gmail.com")
                        .password("password")
                        .roles("USER")
                        .build();

        boolean result = jwtService.isTokenValid(
                token,
                differentUser
        );

        assertFalse(result);
    }

    @Test
    void extractUsername_shouldThrowExceptionForInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername(invalidToken)
        );
    }
}
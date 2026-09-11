package com.example.employeemanagement.globalexceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.exception.ErrorResponse;
import com.example.employeemanagement.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/employees");
    }

    @Test
    void handleEmployeeNotFound_ShouldReturn404() {

        EmployeeNotFoundException exception =
                new EmployeeNotFoundException("Employee not found with id: 99");

        ResponseEntity<ErrorResponse> response =
                handler.handleEmployeeNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().error());
        assertEquals(
                "Employee not found with id: 99",
                response.getBody().message()
        );
        assertEquals(
                "/api/v1/employees",
                response.getBody().path()
        );
    }

    @Test
    void handleDuplicateEmail_ShouldReturn409() {

        DuplicateEmailException exception =
                new DuplicateEmailException(
                        "Employee already exists with email: test@gmail.com"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateEmail(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Duplicate Email", response.getBody().error());
        assertEquals(
                "Employee already exists with email: test@gmail.com",
                response.getBody().message()
        );
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Database constraint violation");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Data Integrity Violation",
                response.getBody().error()
        );
        assertEquals(
                "The request violates a database constraint",
                response.getBody().message()
        );
    }

    @Test
    void handleInvalidJson_ShouldReturn400() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidJson(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Invalid Request Body",
                response.getBody().error()
        );
        assertEquals(
                "Request body is invalid or contains incorrect data",
                response.getBody().message()
        );
    }

    @Test
    void handleTypeMismatch_ShouldReturn400() {

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("id");

        ResponseEntity<ErrorResponse> response =
                handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Invalid Parameter",
                response.getBody().error()
        );
        assertEquals(
                "Invalid value for parameter: id",
                response.getBody().message()
        );
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid employee data");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals(
                "Invalid employee data",
                response.getBody().message()
        );
    }

    @Test
    void handleGeneralException_ShouldReturn500() {

        Exception exception =
                new RuntimeException("Unexpected database error");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneralException(exception, request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(
                "Internal Server Error",
                response.getBody().error()
        );
        assertEquals(
                "An unexpected error occurred",
                response.getBody().message()
        );
    }
}
package com.example.employeemanagement.controllertest;

import com.example.employeemanagement.controller.EmployeeController;
import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private EmployeeRequest getRequest() {

        return new EmployeeRequest(
                "Yashwanth",
                "Kumar",
                "yashwanth@gmail.com",
                "9876543210",
                "IT",
                "Java Developer",
                new BigDecimal("75000"),
                LocalDate.of(2026, 9, 7)
        );
    }

    private EmployeeResponse getResponse() {

        return new EmployeeResponse(
                1L,
                "Yashwanth",
                "Kumar",
                "yashwanth@gmail.com",
                "9876543210",
                "IT",
                "Java Developer",
                new BigDecimal("75000"),
                LocalDate.of(2026, 9, 7)
        );
    }

    @Test
    void createEmployee_shouldReturn201() throws Exception {

        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenReturn(getResponse());

        mockMvc.perform(
                post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRequest()))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("Yashwanth"))
        .andExpect(jsonPath("$.email").value("yashwanth@gmail.com"))
        .andExpect(jsonPath("$.department").value("IT"));

        verify(employeeService)
                .createEmployee(any(EmployeeRequest.class));
    }

    @Test
    void getAllEmployees_shouldReturn200() throws Exception {

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(getResponse()));

        mockMvc.perform(
                get("/api/employees")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].firstName").value("Yashwanth"))
        .andExpect(jsonPath("$[0].email")
                .value("yashwanth@gmail.com"));
    }

    @Test
    void getEmployeeById_shouldReturn200() throws Exception {

        when(employeeService.getEmployeeById(1L))
                .thenReturn(getResponse());

        mockMvc.perform(
                get("/api/employees/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("Yashwanth"));
    }

    @Test
    void getEmployeeById_shouldReturn404() throws Exception {

        when(employeeService.getEmployeeById(999L))
                .thenThrow(
                        new EmployeeNotFoundException(
                                "Employee not found with id: 999"
                        )
                );

        mockMvc.perform(
                get("/api/employees/999")
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateEmployee_shouldReturn200() throws Exception {

        when(employeeService.updateEmployee(
                eq(1L),
                any(EmployeeRequest.class)
        )).thenReturn(getResponse());

        mockMvc.perform(
                put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRequest()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.jobTitle")
                .value("Java Developer"));
    }

    @Test
    void deleteEmployee_shouldReturn204() throws Exception {

        doNothing()
                .when(employeeService)
                .deleteEmployee(1L);

        mockMvc.perform(
                delete("/api/employees/1")
        )
        .andExpect(status().isNoContent());

        verify(employeeService)
                .deleteEmployee(1L);
    }

    @Test
    void createEmployee_shouldReturn400WhenValidationFails()
            throws Exception {

        EmployeeRequest invalidRequest = new EmployeeRequest(
                "",
                "",
                "invalid-email",
                "123",
                "",
                "",
                BigDecimal.ZERO,
                null
        );

        mockMvc.perform(
                post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(invalidRequest)
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error")
                .value("Validation Failed"));

        verify(employeeService, never())
                .createEmployee(any(EmployeeRequest.class));
    }
}
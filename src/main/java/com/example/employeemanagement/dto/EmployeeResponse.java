package com.example.employeemanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(

        Long id,

        String firstName,

        String lastName,

        String email,

        String phoneNumber,

        String department,

        String jobTitle,

        BigDecimal salary,

        LocalDate joiningDate
) {
}
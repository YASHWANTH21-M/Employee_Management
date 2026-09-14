package com.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Optional — only used during /register. Defaults to USER if not provided.
    private String role;
}

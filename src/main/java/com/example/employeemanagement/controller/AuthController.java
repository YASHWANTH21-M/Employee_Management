package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.AuthRequest;
import com.example.employeemanagement.dto.AuthResponse;
import com.example.employeemanagement.entity.AppUser;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ──────────────────────────────────────────────────────────────
    //  POST /api/v1/auth/register
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {

        // Reject duplicate usernames
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, null, "Username already exists"));
        }

        // Default role is USER; pass role=ADMIN in the body to create an admin
        Role role = (request.getRole() != null
                && request.getRole().equalsIgnoreCase("ADMIN"))
                ? Role.ADMIN
                : Role.USER;

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("New user registered: [{}] with role: [{}]", request.getUsername(), role);

        // Build a lightweight UserDetails to generate the token
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(token, role.name(), "User registered successfully"));
    }

    // ──────────────────────────────────────────────────────────────
    //  POST /api/v1/auth/login
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        // authenticate() throws BadCredentialsException if credentials are wrong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");

        log.info("User logged in successfully: [{}]", request.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, role, "Login successful")
        );
    }
}

package com.example.employeemanagement.controllertest;

import com.example.employeemanagement.controller.AuthController;
import com.example.employeemanagement.dto.AuthRequest;
import com.example.employeemanagement.entity.AppUser;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;


    // ---------------------------------------------------------
    // REGISTER - USER
    // ---------------------------------------------------------

    @Test
    void register_shouldCreateUserWithUserRole()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("yashwanth@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername(
                "yashwanth@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        AppUser savedUser = AppUser.builder()
                .id(1L)
                .username("yashwanth@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("test-jwt-token");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token")
                .value("test-jwt-token"))
        .andExpect(jsonPath("$.role")
                .value("USER"))
        .andExpect(jsonPath("$.message")
                .value("User registered successfully"));

        verify(userRepository)
                .existsByUsername("yashwanth@gmail.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(AppUser.class));

        verify(jwtService)
                .generateToken(any(UserDetails.class));
    }


    // ---------------------------------------------------------
    // REGISTER - ADMIN
    // ---------------------------------------------------------

    @Test
    void register_shouldCreateAdminWhenRoleIsAdmin()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("admin@gmail.com");
        request.setPassword("admin123");
        request.setRole("ADMIN");

        when(userRepository.existsByUsername("admin@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("admin123"))
                .thenReturn("encodedAdminPassword");

        AppUser savedUser = AppUser.builder()
                .id(2L)
                .username("admin@gmail.com")
                .password("encodedAdminPassword")
                .role(Role.ADMIN)
                .build();

        when(userRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("admin-jwt-token");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token")
                .value("admin-jwt-token"))
        .andExpect(jsonPath("$.role")
                .value("ADMIN"))
        .andExpect(jsonPath("$.message")
                .value("User registered successfully"));

        verify(userRepository)
                .save(any(AppUser.class));
    }


    // ---------------------------------------------------------
    // REGISTER - DUPLICATE USERNAME
    // ---------------------------------------------------------

    @Test
    void register_shouldReturn409WhenUsernameExists()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("existing@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername(
                "existing@gmail.com"))
                .thenReturn(true);

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(jsonPath("$.role").doesNotExist())
        .andExpect(jsonPath("$.message")
                .value("Username already exists"));

        verify(userRepository)
                .existsByUsername("existing@gmail.com");

        verify(userRepository, never())
                .save(any(AppUser.class));

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(jwtService, never())
                .generateToken(any(UserDetails.class));
    }


    // ---------------------------------------------------------
    // LOGIN - SUCCESS
    // ---------------------------------------------------------

    @Test
    void login_shouldReturnToken()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("yashwanth@gmail.com");
        request.setPassword("password123");

        UserDetails userDetails =
                User.withUsername("yashwanth@gmail.com")
                        .password("encodedPassword")
                        .roles("USER")
                        .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(userDetails))
                .thenReturn("login-jwt-token");

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token")
                .value("login-jwt-token"))
        .andExpect(jsonPath("$.role")
                .value("USER"))
        .andExpect(jsonPath("$.message")
                .value("Login successful"));

        verify(authenticationManager)
                .authenticate(any(
                        UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken(userDetails);
    }


    // ---------------------------------------------------------
    // LOGIN - ADMIN
    // ---------------------------------------------------------

    @Test
    void login_shouldReturnAdminRole()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("admin@gmail.com");
        request.setPassword("admin123");

        UserDetails adminDetails =
                User.withUsername("admin@gmail.com")
                        .password("encodedPassword")
                        .roles("ADMIN")
                        .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        adminDetails,
                        null,
                        adminDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(adminDetails))
                .thenReturn("admin-jwt-token");

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token")
                .value("admin-jwt-token"))
        .andExpect(jsonPath("$.role")
                .value("ADMIN"))
        .andExpect(jsonPath("$.message")
                .value("Login successful"));
    }


    // ---------------------------------------------------------
    // VALIDATION - EMPTY USERNAME
    // ---------------------------------------------------------

    @Test
    void register_shouldReturn400WhenUsernameIsEmpty()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("password123");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status")
                .value(400))
        .andExpect(jsonPath("$.error")
                .value("Validation Failed"));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }


    // ---------------------------------------------------------
    // VALIDATION - EMPTY PASSWORD
    // ---------------------------------------------------------

    @Test
    void login_shouldReturn400WhenPasswordIsEmpty()
            throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("yashwanth@gmail.com");
        request.setPassword("");

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status")
                .value(400))
        .andExpect(jsonPath("$.error")
                .value("Validation Failed"));

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtService);
    }
}
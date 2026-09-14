package com.example.employeemanagement.config;

import com.example.employeemanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    // ──────────────────────────────────────────────────────────────
    //  Security Filter Chain — defines all authorization rules
    // ──────────────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless REST APIs)
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS (configured below for React frontend)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints are public (register & login)
                .requestMatchers("/api/v1/auth/**").permitAll()
                // H2 console allowed in dev
                .requestMatchers("/h2-console/**").permitAll()
                // GET requests — both USER and ADMIN can read
                .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole("USER", "ADMIN")
                // Write operations — ADMIN only
                .requestMatchers(HttpMethod.POST,   "/api/v1/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/v1/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Stateless session (JWT handles state, no HTTP sessions)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Use our custom DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Insert JWT filter before the default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Allow H2 console to render inside an iframe
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    // ──────────────────────────────────────────────────────────────
    //  CORS — allows React frontend at localhost:3000
    // ──────────────────────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ──────────────────────────────────────────────────────────────
    //  Authentication Provider — wires UserDetailsService + encoder
    // ──────────────────────────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Security 7.x: UserDetailsService is passed in the constructor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ──────────────────────────────────────────────────────────────
    //  AuthenticationManager — used by AuthController for login
    // ──────────────────────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ──────────────────────────────────────────────────────────────
    //  Password Encoder — BCrypt
    // ──────────────────────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

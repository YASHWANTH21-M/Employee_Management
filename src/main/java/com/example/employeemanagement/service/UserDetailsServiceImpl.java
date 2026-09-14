package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.AppUser;
import com.example.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found in database: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }
}

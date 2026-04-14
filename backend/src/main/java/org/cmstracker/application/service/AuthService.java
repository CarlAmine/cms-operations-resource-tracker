package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.domain.model.User;
import org.cmstracker.domain.repository.UserRepository;
import org.cmstracker.infrastructure.exception.ConflictException;
import org.cmstracker.infrastructure.exception.ResourceNotFoundException;
import org.cmstracker.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        String token = tokenProvider.generateToken(auth.getName());
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getUsername()));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserDTO(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .map(this::toUserDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private UserDTO toUserDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole().name())
                .enabled(u.isEnabled())
                .build();
    }
}

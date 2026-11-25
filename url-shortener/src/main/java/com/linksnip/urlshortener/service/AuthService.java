package com.linksnip.urlshortener.service;

import com.linksnip.urlshortener.dto.request.LoginRequest;
import com.linksnip.urlshortener.dto.request.RegisterRequest;
import com.linksnip.urlshortener.dto.response.AuthResponse;
import com.linksnip.urlshortener.entity.User;
import com.linksnip.urlshortener.exception.EmailAlreadyRegisteredException;
import com.linksnip.urlshortener.repository.UserRepository;
import com.linksnip.urlshortener.security.JwtService;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException("Email already registered: " + request.email());
        }

        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .createdAt(Instant.now())
            .build();

        userRepository.save(user);

        return issueAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return issueAuthResponse(user);
    }

    private AuthResponse issueAuthResponse(User user) {
        String token = jwtService.issueToken(user.getId(), user.getEmail());
        return new AuthResponse(token, "Bearer", expirationMs / 1000);
    }
}

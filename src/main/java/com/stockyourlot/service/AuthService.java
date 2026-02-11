package com.stockyourlot.service;

import com.stockyourlot.config.JwtUtil;
import com.stockyourlot.dto.*;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResult register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return RegisterResult.conflict("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            return RegisterResult.conflict("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), passwordHash, "USER");
        user = userRepository.save(user);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "Registration successful"
        );
        return RegisterResult.success(response);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());
        return new LoginResponse("Login successful", user.getUsername(), user.getEmail(), token);
    }
}

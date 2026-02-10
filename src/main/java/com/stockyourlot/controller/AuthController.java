package com.stockyourlot.controller;

import com.stockyourlot.dto.RegisterRequest;
import com.stockyourlot.dto.RegisterResponse;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegisterResponse(null, null, null, "Username already taken"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegisterResponse(null, null, null, "Email already registered"));
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), passwordHash, "USER");
        user = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterResponse(user.getId(), user.getUsername(), user.getEmail(), "Registration successful")
        );
    }
}

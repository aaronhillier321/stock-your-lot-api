package com.stockyourlot.controller;

import com.stockyourlot.dto.RegisterRequest;
import com.stockyourlot.dto.RegisterResponse;
import com.stockyourlot.dto.RegisterResult;
import com.stockyourlot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResult result = authService.register(request);
        if (result.success()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.response());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RegisterResponse(null, null, null, result.errorMessage()));
    }
}

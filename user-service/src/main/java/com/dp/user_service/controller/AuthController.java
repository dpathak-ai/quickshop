package com.dp.user_service.controller;

import com.dp.user_service.dto.AuthResponse;
import com.dp.user_service.dto.LoginRequest;
import com.dp.user_service.dto.RefreshTokenRequest;
import com.dp.user_service.dto.RegisterRequest;
import com.dp.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid RegisterRequest registerRequest){
        log.info("Registering user with email: {}", registerRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest){
        log.info("Logging in user with email: {}", loginRequest.getEmail());
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        log.info("Refreshing token");
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}

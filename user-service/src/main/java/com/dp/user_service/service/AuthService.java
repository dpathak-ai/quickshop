package com.dp.user_service.service;

import com.dp.user_service.dto.AuthResponse;
import com.dp.user_service.dto.LoginRequest;
import com.dp.user_service.dto.RefreshTokenRequest;
import com.dp.user_service.dto.RegisterRequest;
import com.dp.user_service.exception.InvalidTokenException;
import com.dp.user_service.exception.UserAlreadyExistsException;
import com.dp.user_service.model.RefreshToken;
import com.dp.user_service.model.User;
import com.dp.user_service.repository.RefreshTokenRepository;
import com.dp.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final KafkaProducerService kafkaProducerService;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registering failed - User with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Objects.isNull(request.getRole()) ? User.Role.USER : request.getRole())
                .build();

        user = userRepository.save(user);
        log.debug("User registered successfully with id: {}", user.getId());

        kafkaProducerService.publishUserRegistered(user);
        log.info("User registered successfully - userId: {}, email: {}",
                user.getId(), user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - Invalid credentials for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);
        log.info("Login successful - userId: {}, email: {}", user.getId(), user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);

    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = storedToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);

        return buildAuthResponse(user, accessToken, storedToken.getToken());
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }
}

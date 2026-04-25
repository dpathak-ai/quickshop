package com.dp.user_service.repository;

import com.dp.user_service.model.RefreshToken;
import com.dp.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
    void deleteByUser(User user);

}

package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByAuthUser(AuthUser user);
}
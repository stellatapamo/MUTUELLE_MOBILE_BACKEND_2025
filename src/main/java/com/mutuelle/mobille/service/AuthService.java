package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.LoginResponseDto;
import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.models.RefreshToken;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepo;
    private final MemberRepository memberRepo;
    private final AdminRepository adminRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(String email, String password) {
        AuthUser authUser = authUserRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));

        if (!passwordEncoder.matches(password, authUser.getPasswordHash())) {
            throw new BadCredentialsException("Identifiants invalides");
        }

        String accessToken = jwtUtils.generateAccessToken(authUser);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .authUser(authUser)
                .expiryDate(Instant.now().plusMillis(jwtUtils.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshToken);

        Object profile = switch (authUser.getUserType()) {
            case MEMBER -> memberRepo.findById(authUser.getUserRefId()).orElse(null);
            case ADMIN -> adminRepo.findById(authUser.getUserRefId()).orElse(null);
//            default -> null;
        };

        return LoginResponseDto.builder()
                .userType(authUser.getUserType())
                .userRefId(authUser.getUserRefId())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .profile(profile)
                .build();
    }

    public String refresh(String refreshTokenValue) {
        RefreshToken rt = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));

        if (rt.isRevoked() || rt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expiré ou révoqué");
        }

        return jwtUtils.generateAccessToken(rt.getAuthUser());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepo.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
        });
    }
}
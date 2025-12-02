package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.models.RefreshToken;
import com.mutuelle.mobille.repository.AdminRepository;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.RefreshTokenRepository;
import com.mutuelle.mobille.util.JwtUtils;
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

    public LoginResponse login(String email, String password) {
        AuthUser au = authUserRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(password, au.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtils.generateAccessToken(au);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setAuthUser(au);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtils.getRefreshExpirationMs()));
        refreshTokenRepo.save(refreshToken);

        // load profile minimal
        Object profile = null;
        if ("MEMBER".equals(au.getUserType())) profile = memberRepo.findById(au.getUserRefId()).orElse(null);
        else profile = adminRepo.findById(au.getUserRefId()).orElse(null);

        return new LoginResponse(au.getUserType(), au.getUserRefId(), accessToken, refreshTokenValue, profile);
    }

    public String refresh(String refreshTokenValue) {
        RefreshToken rt = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        AuthUser au = rt.getAuthUser();
        return jwtUtils.generateAccessToken(au);
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepo.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
        });
    }
}

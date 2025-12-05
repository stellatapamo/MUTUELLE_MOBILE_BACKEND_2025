package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.auth.LoginResponseDto;
import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.models.RefreshToken;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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

        Object profile = switch (authUser.getRole()) {
            case MEMBER -> memberRepo.findById(authUser.getUserRefId()).orElse(null);
            case ADMIN -> adminRepo.findById(authUser.getUserRefId()).orElse(null);
            case SUPER_ADMIN -> adminRepo.findById(authUser.getUserRefId()).orElse(null);
//            default -> null;
        };

        return LoginResponseDto.builder()
                .userType(authUser.getRole())
                .userRefId(authUser.getUserRefId())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .profile(profile)
                .build();
    }

    public String refresh(String refreshTokenValue) {
        RefreshToken rt = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide ou inconnu"));

        if (rt.isRevoked()) {
            throw new RuntimeException("Refresh token déjà révoqué");
        }
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
            throw new RuntimeException("Refresh token expiré");
        }

        // Optionnel : politique "single-use" (recommandé)
        rt.setRevoked(true);
        refreshTokenRepo.save(rt);

        return jwtUtils.generateAccessToken(rt.getAuthUser());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepo.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
        });
    }

    public void logoutCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            // Si on arrive ici sans être authentifié - rien à faire (ou loguer)
            return;
        }

        // Révoque TOUS les refresh tokens actifs de cet utilisateur
        List<RefreshToken> activeTokens = refreshTokenRepo.findAllByAuthUserAndRevokedFalse(user);

        if (!activeTokens.isEmpty()) {
            activeTokens.forEach(rt -> rt.setRevoked(true));
            refreshTokenRepo.saveAll(activeTokens);
        }
    }
}
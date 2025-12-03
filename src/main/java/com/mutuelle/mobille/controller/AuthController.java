package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.*;
import com.mutuelle.mobille.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion de l'utilisateur")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/refresh")
//    @Operation(summary = "Rafraîchir le token d'accès")
//    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody TokenResponseDto request) {
//        String newAccessToken = authService.refresh(request.getRefreshToken());
//        return ResponseEntity.ok(new TokenResponseDto(newAccessToken));
//    }

//    @PostMapping("/logout")
//    @Operation(summary = "Déconnexion (invalide le refresh token)")
//    public ResponseEntity<Void> logout(@Valid @RequestBody TokenResponseDto request) {
//        authService.logout(request.getRefreshToken());
//        return ResponseEntity.ok().build();
//    }
}
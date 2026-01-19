package com.mutuelle.mobille.controller;


import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.notifications.PushTestDto;
import com.mutuelle.mobille.dto.notifications.PushTokenDto;
import com.mutuelle.mobille.service.notifications.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/token")
    @Operation(summary = "Enregistrer un nouveau token push pour l'utilisateur connecté (support multi-appareils)")
    public ResponseEntity<ApiResponseDto<Void>> registerToken(@Valid @RequestBody PushTokenDto dto) {
        pushNotificationService.registerToken(dto);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Token push enregistré avec succès"));
    }

    @PostMapping("/test")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envoyer une notification push de TEST à TOUS les appareils d'un utilisateur donné (admin only)")
    public ResponseEntity<ApiResponseDto<Void>> testPushToUser(@Valid @RequestBody PushTestDto dto) {
        pushNotificationService.sendTestPushToUser(dto.getUserId(), dto.getTitle(), dto.getMessage());
        return ResponseEntity.ok(ApiResponseDto.ok(null,
                "Notification de test envoyée à l'utilisateur ID " + dto.getUserId() + " (tous appareils)"));
    }
}
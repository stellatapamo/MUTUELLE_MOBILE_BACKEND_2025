package com.mutuelle.mobille.controller;


import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.dto.notifications.PushTestDto;
import com.mutuelle.mobille.dto.notifications.PushTokenDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import com.mutuelle.mobille.enums.TemplateMailsName;
import com.mutuelle.mobille.service.notifications.config.NotificationService;
import com.mutuelle.mobille.service.notifications.config.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/notifications/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;
    private final NotificationService notificationService;

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

    @PostMapping("/test/mail")
    @Operation(summary = "Envoyer une notification mail de TEST")
    public ResponseEntity<ApiResponseDto<Void>> testMail(
            @Valid @RequestBody Map<String, String> payload) {

        try {
            // Extraction des champs (pas de DTO → on récupère via Map)
            String email  = payload.get("email");
            String subject = payload.get("subject");   // corrigé : plus de "subjet"
            String body   = payload.get("body");

            // Validation manuelle rapide (puisque @Valid ne fonctionne pas bien sur Map brut)
            if (email == null || email.trim().isEmpty() ||
                    subject == null || subject.trim().isEmpty() ||
                    body == null || body.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDto.error("Les champs email, subject et body sont obligatoires"));
            }

            // Préparation des variables pour Thymeleaf
            Map<String, Object> thymeleafVars = new HashMap<>();
            thymeleafVars.put("userName", "Test User");
            thymeleafVars.put("currentTime", LocalDateTime.now().toString());
            thymeleafVars.put("messageCustom", body);  // on passe le body dans le template si besoin

            // Construction de la notification
            NotificationRequestDto request = NotificationRequestDto.builder()
                    .email(email)
                    .title(subject)
                    .templateName(TemplateMailsName.WELCOME)   // ou un autre template
                    .variables(thymeleafVars)
                    .channels(Set.of(NotificationChannel.EMAIL))  // on enlève le PUSH
                    .message(body)  // fallback si le template plante
                    .build();

            notificationService.sendNotification(request);

            String successMsg = "Email de test envoyé avec succès à : " + email;
            return ResponseEntity.ok(ApiResponseDto.ok(null, successMsg));

        } catch (Exception e) {
            // À remplacer par ton logger (Slf4j, Logback, etc.)
            System.err.println("Échec envoi email test : " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erreur lors de l'envoi : " + e.getMessage()));
        }
    }
}
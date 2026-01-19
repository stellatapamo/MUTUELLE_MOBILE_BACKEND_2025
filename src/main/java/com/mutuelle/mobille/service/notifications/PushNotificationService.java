package com.mutuelle.mobille.service.notifications;


import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.dto.notifications.PushTestDto;
import com.mutuelle.mobille.dto.notifications.PushTokenDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.models.PushToken;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.repository.PushTokenRepository;
import com.mutuelle.mobille.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushTokenRepository pushTokenRepository;
    private final AuthUserRepository authUserRepository;
    private final AuthService authService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    /**
     * Enregistre ou met à jour le token pour l'utilisateur courant
     * (pas de doublon par utilisateur + token)
     */
    public void registerToken(PushTokenDto dto) {
        Optional<AuthUser> currentUserOpt = authService.getCurrentUser();
        if (currentUserOpt.isEmpty()){
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }
        AuthUser currentUser=currentUserOpt.get();
        String token = dto.getToken().trim();

        // Vérifier existence
        boolean exists = pushTokenRepository.findByUser(currentUser).stream()
                .anyMatch(t -> t.getToken().equals(token));

        if (exists) {
            log.info("Token push déjà enregistré pour {}", currentUser.getEmail());
            return;
        }

        PushToken pushToken = new PushToken();
        pushToken.setToken(token);
        pushToken.setUser(currentUser);
        pushTokenRepository.save(pushToken);

        log.info("Nouveau token push enregistré pour {} → {}", currentUser.getEmail(), token);
    }

    /**
     * Envoi réel vers Expo + nettoyage automatique des tokens invalides
     */
    public void sendPush(NotificationRequestDto req) {
        if (req.getEmail() == null) {
            log.warn("Impossible d'envoyer push : email manquant dans la request");
            return;
        }

        AuthUser user = authUserRepository.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null) {
            log.warn("Utilisateur non trouvé pour email: {}", req.getEmail());
            return;
        }

        List<PushToken> tokens = pushTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.info("Aucun token push enregistré pour {}", req.getEmail());
            return;
        }

        // Préparation du batch Expo
        List<Map<String, Object>> messages = new ArrayList<>();
        for (PushToken pt : tokens) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("to", pt.getToken());
            msg.put("title", req.getTitle() != null ? req.getTitle() : "Notification");
            msg.put("body", req.getMessage() != null ? req.getMessage() : "");
            if (req.getVariables() != null && !req.getVariables().isEmpty()) {
                msg.put("data", req.getVariables());
            }
            // Optionnel : sound, priority, badge, etc.
            messages.add(msg);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(messages, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    EXPO_PUSH_URL, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object dataObj = response.getBody().get("data");
                if (dataObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) dataObj;
                    handlePushResults(results, tokens);
                }
            } else {
                log.error("Expo API erreur HTTP {} - {}", response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'appel Expo push", e);
        }
    }

    /**
     * Analyse des résultats Expo et suppression automatique des tokens invalides
     */
    @SuppressWarnings("unchecked")
    private void handlePushResults(List<Map<String, Object>> results, List<PushToken> sentTokens) {
        for (int i = 0; i < results.size() && i < sentTokens.size(); i++) {
            Map<String, Object> result = results.get(i);
            String status = (String) result.get("status");

            if ("error".equals(status)) {
                String errorCode = null;
                Map<String, Object> details = (Map<String, Object>) result.get("details");
                if (details != null) {
                    errorCode = (String) details.get("error");
                }

                // Les cas où on supprime définitivement le token
                if ("DeviceNotRegistered".equals(errorCode)) {
                    String badToken = sentTokens.get(i).getToken();
                    pushTokenRepository.deleteByToken(badToken);
                    log.warn("Token supprimé (DeviceNotRegistered) → {}", badToken);
                }
                // Vous pouvez ajouter d'autres codes si besoin : "MessageRateExceeded", "InvalidCredentials", etc.
                // Mais pour l'instant on se concentre sur DeviceNotRegistered
            }
        }
    }

    public void sendTestPush(PushTestDto dto) {
        Optional<AuthUser> currentUserOpt = authService.getCurrentUser();
        if (currentUserOpt.isEmpty()){
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }
        AuthUser current=currentUserOpt.get();

        NotificationRequestDto req = NotificationRequestDto.builder()
                .email(current.getEmail())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .channels(Set.of(NotificationChannel.PUSH))
                .build();

        sendPush(req);
    }

    /**
     * Envoi d'une notification de test à un utilisateur spécifique (par ID) → tous ses tokens/appareils
     * @param userId ID de l'AuthUser (ou Member, selon ta logique)
     * @param title
     * @param message
     */
    public void sendTestPushToUser(Long userId, String title, String message) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + userId));

        // Récupère tous les tokens de cet utilisateur
        List<PushToken> tokens = pushTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.warn("Aucun token push pour l'utilisateur ID {} ({})", userId, user.getEmail());
            throw new RuntimeException("Aucun appareil enregistré pour cet utilisateur");
        }

        NotificationRequestDto req = NotificationRequestDto.builder()
                .email(user.getEmail())           // requis pour la cohérence avec sendPush
                .title(title)
                .message(message)
                .channels(Set.of(NotificationChannel.PUSH))
                .build();

        // On réutilise la méthode existante sendPush qui gère déjà le batch + nettoyage auto
        sendPush(req);

        log.info("Test push envoyé à {} appareils pour utilisateur ID {} ({})",
                tokens.size(), userId, user.getEmail());
    }
}


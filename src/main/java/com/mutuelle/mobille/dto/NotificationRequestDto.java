package com.mutuelle.mobille.dto;

import com.mutuelle.mobille.enums.NotificationChannel;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Builder
@Getter
public class NotificationRequestDto {
    private final String email;               // ou email, phone selon besoin
    private final String title;
    private final String message;              // version texte simple (fallback ou push)
    private final String templateName;         // pour email HTML
    private final Map<String, Object> variables; // pour Thymeleaf
    @Builder.Default
    private final Set<NotificationChannel> channels = Set.of(NotificationChannel.EMAIL); // EMAIL, PUSH, ...
    private final Map<String, Object> pushData;
}

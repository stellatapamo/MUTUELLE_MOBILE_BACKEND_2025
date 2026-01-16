package com.mutuelle.mobille.service.notifications;

import com.mutuelle.mobille.dto.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRequestDto req) {
        // TODO: implémenter FCM / OneSignal / APNs quand prêt
        log.info("PUSH envoyé (simulation) → User: {}, Title: {}, Message: {}",
                req.getEmail(), req.getTitle(), req.getMessage());

        // Exemple futur :
        // fcmService.sendToUser(req.getUserId(), req.getTitle(), req.getMessage(), req.getPushData());
    }
}
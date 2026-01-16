package com.mutuelle.mobille.service.notifications;

import com.mutuelle.mobille.dto.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;

public interface NotificationSender {

    NotificationChannel getChannel();

    void send(NotificationRequestDto request);
}
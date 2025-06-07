package org.gcp.smartnotify.service.impl;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.service.BotSender;
import org.gcp.smartnotify.service.NotificationSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotification implements NotificationSender {

  private final BotSender botSender;

  @Override
  public void send(NotificationEvent event) {
    botSender.send(event.getUserId(), event.getMessage());
  }
}

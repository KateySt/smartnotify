package org.gcp.smartnotify.service;


import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumer {

  private final NotificationService notificationService;

  public NotificationEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @KafkaListener(topics = "notifications", groupId = "smart-notify-group", containerFactory = "kafkaListenerContainerFactory")
  public void consume(NotificationEvent event) {
    notificationService.dispatch(event);
  }
}
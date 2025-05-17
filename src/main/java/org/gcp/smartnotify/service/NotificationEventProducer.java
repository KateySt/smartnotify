package org.gcp.smartnotify.service;

import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventProducer {

  private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

  public NotificationEventProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendEvent(NotificationEvent event) {
    kafkaTemplate.send("notifications", event);
  }
}
package org.gcp.smartnotify.controller;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.service.NotificationEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationEventProducer eventProducer;

  @PostMapping
  public ResponseEntity<Void> sendNotification(@RequestBody NotificationEvent event) {
    eventProducer.sendEvent(event);
    return ResponseEntity.accepted().build();
  }
}
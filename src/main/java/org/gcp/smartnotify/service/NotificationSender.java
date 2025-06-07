package org.gcp.smartnotify.service;

import org.gcp.smartnotify.model.dto.NotificationEvent;

public interface NotificationSender {
  void send(NotificationEvent event);
}
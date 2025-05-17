package org.gcp.smartnotify.service.impl;

import org.gcp.smartnotify.model.dto.NotificationEvent;

public interface NotificationSender {
  void send(NotificationEvent event);
}
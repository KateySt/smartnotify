package org.gcp.smartnotify.service;

import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.service.impl.NotificationSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

  private final Map<Channel, NotificationSender> senders;

  public NotificationService(List<NotificationSender> senderList) {
    this.senders = senderList.stream().collect(Collectors.toMap(
        sender -> resolveChannel(sender.getClass()),
        Function.identity()
    ));
  }

  public void dispatch(NotificationEvent event) {
    final NotificationSender sender = senders.get(event.getType());
    if (sender != null) {
      sender.send(event);
    } else {
      throw new IllegalArgumentException("Unsupported channel: " + event.getType());
    }
  }

  private Channel resolveChannel(Class<?> clazz) {
    if (clazz.isAssignableFrom(TelegramNotification.class)) {
      return Channel.TELEGRAM;
    }
    if (clazz.isAssignableFrom(EmailNotification.class)) {
      return Channel.EMAIL;
    }
    if (clazz.isAssignableFrom(SmsNotification.class)) {
      return Channel.SMS;
    }
    throw new IllegalStateException("Unknown sender type: " + clazz.getName());
  }
}

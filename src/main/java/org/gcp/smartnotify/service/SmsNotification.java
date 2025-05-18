package org.gcp.smartnotify.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.service.impl.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsNotification implements NotificationSender {

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;

  @Value("${twilio.phoneNumber}")
  private String fromNumber;

  @Override
  public void send(NotificationEvent event) {
    try {
      Twilio.init(accountSid, authToken);
      log.info("SMS sent successfully to {}. SID: {}", event.getTo(), fromNumber);
      final Message message = Message.creator(
          new PhoneNumber(event.getTo()),
          new PhoneNumber(fromNumber),
          event.getMessage()
      ).create();

      log.info("SMS sent successfully to {}. SID: {}", event.getTo(), message.getSid());
    } catch (Exception e) {

      log.error("Failed to send SMS to {}: {}", event.getTo(), e.getMessage(), e);
    }
  }
}
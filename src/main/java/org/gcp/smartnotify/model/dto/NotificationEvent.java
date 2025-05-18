package org.gcp.smartnotify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gcp.smartnotify.enums.Channel;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  private String userId;
  private String message;
  private Instant scheduledAt;
  private Channel channel;
  private Long telegramId;

  private String to;
  private String subject;
  private String templateName;
  private Map<String, Object> variables;
}
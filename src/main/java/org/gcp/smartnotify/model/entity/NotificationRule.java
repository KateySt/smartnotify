package org.gcp.smartnotify.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.gcp.smartnotify.enums.Channel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@Document(collection = "notification-rule")
public class NotificationRule {
  @Id
  private String id;
  private Long telegramId;
  private Instant scheduledAt;
  private List<Channel> channel;
  private String message;
  private Map<String, String> filters;
  private boolean active;
}

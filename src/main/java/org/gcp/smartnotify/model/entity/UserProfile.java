package org.gcp.smartnotify.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@Document(collection = "users")
public class UserProfile {
  @Id
  private String id;
  private Long telegramId;
  private String userName;
  private String languageCode;
  private String email;
  private String phone;

  private Instant emailVerificationCreatedAt;
  private boolean emailVerified;
  private String emailVerificationCode;
  
  private boolean enabled;
}
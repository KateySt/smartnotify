package org.gcp.smartnotify.service;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.model.entity.UserProfile;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

  private final NotificationRuleRepository ruleRepository;
  private final UserProfileRepository userRepository;
  private final NotificationService notificationService;

  @Scheduled(cron = "0 0 * * * *")
  public void checkAndNotify() {
    final Instant now = Instant.now();

    ruleRepository.findAllByScheduledAtBeforeAndActiveTrue(now)
        .flatMap(rule ->
            userRepository.findByTelegramId(rule.getTelegramId())
                .flatMapMany(user ->
                    Flux.fromIterable(rule.getChannel())
                        .flatMap(channel -> {
                          final NotificationEvent event = buildScheduledEvent(rule, user, channel);
                          return Mono.fromRunnable(() -> notificationService.dispatch(event)).then();
                        })
                )
        )
        .subscribe();
  }

  private NotificationEvent buildScheduledEvent(NotificationRule rule, UserProfile user, Channel channel) {
    return NotificationEvent.builder()
        .channel(channel)
        .telegramId(user.getTelegramId())
        .message(rule.getMessage())
        .build();
  }
}
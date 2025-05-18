package org.gcp.smartnotify.service;


import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.model.entity.UserProfile;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

  private final NotificationRuleRepository ruleRepository;
  private final UserProfileRepository userRepository;
  private final NotificationService notificationService;

  @KafkaListener(topics = "notifications", containerFactory = "kafkaListenerContainerFactory")
  public void consume(NotificationEvent incomingEvent) {
    ruleRepository.findByScheduledAt(incomingEvent.getScheduledAt())
        .filter(NotificationRule::isActive)
        .flatMap(rule ->
            userRepository.findByTelegramId(rule.getTelegramId())
                .flatMapMany(user ->
                    Flux.fromIterable(rule.getChannel())
                        .flatMap(channel -> {
                          final NotificationEvent notifyEvent = buildChannelEvent(incomingEvent, user, channel);
                          return Mono.fromRunnable(() -> notificationService.dispatch(notifyEvent)).then();
                        })
                )
        )
        .subscribe();
  }

  private NotificationEvent buildChannelEvent(NotificationEvent base,
                                              UserProfile user,
                                              Channel channel) {
    final NotificationEvent.NotificationEventBuilder builder = NotificationEvent.builder()
        .userId(user.getTelegramId().toString())
        .message(base.getMessage())
        .channel(channel);

    switch (channel) {
      case EMAIL:
        builder.to(user.getEmail())
            .subject("Notification: " + base.getSubject())
            .templateName("generic-notification")
            .variables(base.getVariables());
        break;
      case SMS:
        builder.to(user.getPhone());
        break;
      case TELEGRAM:
        break;
    }

    return builder.build();
  }
}
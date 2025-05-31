package org.gcp.smartnotify.repo;

import org.gcp.smartnotify.model.entity.NotificationRule;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface NotificationRuleRepository extends ReactiveMongoRepository<NotificationRule, String> {
  Mono<NotificationRule> findByTelegramId(Long telegramId);

  Flux<NotificationRule> findByScheduledAt(Instant scheduledAt);

  Flux<NotificationRule> findAllByScheduledAtBeforeAndActiveTrue(Instant before);

  Flux<NotificationRule> findAllByTelegramId(Long chartId);
}
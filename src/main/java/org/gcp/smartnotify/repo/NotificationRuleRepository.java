package org.gcp.smartnotify.repo;

import org.gcp.smartnotify.model.entity.NotificationRule;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRuleRepository extends ReactiveMongoRepository<NotificationRule, String> {
  Mono<NotificationRule> findByTelegramId(Long telegramId);
}
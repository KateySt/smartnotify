package org.gcp.smartnotify.repo;

import org.gcp.smartnotify.model.entity.UserProfile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserProfileRepository extends ReactiveMongoRepository<UserProfile, String> {
  Mono<UserProfile> findByTelegramId(Long telegramId);
}

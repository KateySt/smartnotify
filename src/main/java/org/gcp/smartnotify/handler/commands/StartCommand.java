package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.model.entity.UserProfile;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class StartCommand implements org.gcp.smartnotify.handler.Command {

  private final MessageService messageService;
  private final UserProfileRepository repository;
  private final NotificationRuleRepository notificationRuleRepository;

  @Override
  public void execute(Update update) {
    final Long telegramId = update.getMessage().getFrom().getId();
    final String chatId = update.getMessage().getChatId().toString();
    repository.findByTelegramId(telegramId)
        .switchIfEmpty(saveNewUser(update))
        .subscribe(
            user -> {
            },
            error -> {
              log.info("Error while saving user: " + error.getMessage());
            }
        );

    messageService.sendMessage(chatId, BotCommandType.START.getMessage());
  }

  private Mono<UserProfile> saveNewUser(Update update) {
    final var from = update.getMessage().getFrom();

    if (from.getIsBot()) {
      return Mono.error(new IllegalArgumentException("Bots are not allowed to register."));
    }

    notificationRuleRepository.save(NotificationRule.builder()
        .telegramId(from.getId())
        .channel(List.of(Channel.TELEGRAM))
        .build());

    return repository.save(UserProfile.builder()
        .telegramId(from.getId())
        .userName(from.getUserName())
        .languageCode(from.getLanguageCode())
        .email(null)
        .phone(null)
        .enabled(true)
        .build());
  }

  @Override
  public String getCommand() {
    return BotCommandType.START.getCommand();
  }
}
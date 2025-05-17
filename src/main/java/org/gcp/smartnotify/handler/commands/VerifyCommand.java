package org.gcp.smartnotify.handler.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyCommand implements org.gcp.smartnotify.handler.commands.Command {

  private final UserProfileRepository repository;
  private final MessageService messageService;

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final Long telegramId = update.getMessage().getFrom().getId();
    final String[] parts = update.getMessage().getText().trim().split("\\s+");

    if (parts.length < 2) {
      messageService.sendMessage(chatId, "Please enter the verification code like this: /verify 123ABC");
      return;
    }

    final String codeFromUser = parts[1].trim();

    repository.findByTelegramId(telegramId)
        .flatMap(profile -> {
          final String code = profile.getEmailVerificationCode();
          final Instant createdAt = profile.getEmailVerificationCreatedAt();

          if (code == null || createdAt == null) {
            messageService.sendMessage(chatId, "No verification code found. Please request a new one using /email.");
            return Mono.empty();
          }

          final Instant now = Instant.now();
          final Duration duration = Duration.between(createdAt, now);

          if (duration.toMinutes() > 10) {
            profile.setEmailVerificationCode(null);
            profile.setEmailVerificationCreatedAt(null);
            return repository.save(profile)
                .doOnSuccess(p -> messageService.sendMessage(chatId, "Verification code expired. Please request a new one using /email."))
                .then();
          }

          if (!code.equalsIgnoreCase(codeFromUser)) {
            messageService.sendMessage(chatId, "Invalid verification code.");
            return Mono.empty();
          }

          profile.setEmailVerified(true);
          profile.setEmailVerificationCode(null);
          profile.setEmailVerificationCreatedAt(null);

          return repository.save(profile)
              .doOnSuccess(p -> messageService.sendMessage(chatId, "Email verified successfully!"));
        })
        .subscribe();
  }

  @Override
  public String getCommand() {
    return BotCommandType.EMAIL_VERIFY.getCommand();
  }
}

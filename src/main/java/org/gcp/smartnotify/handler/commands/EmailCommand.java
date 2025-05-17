package org.gcp.smartnotify.handler.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.gcp.smartnotify.service.EmailVerificationService;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCommand implements org.gcp.smartnotify.handler.commands.Command {

  private final UserProfileRepository repository;
  private final EmailVerificationService emailService;
  private final MessageService messageService;

  private final Map<String, Boolean> awaitingEmail = new ConcurrentHashMap<>();

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final Long telegramId = update.getMessage().getFrom().getId();
    final String messageText = update.getMessage().getText();

    if (messageText.equalsIgnoreCase(getCommand())) {
      messageService.sendMessage(chatId, "Please send your email address.");
      awaitingEmail.put(chatId, true);
      return;
    }

    if (awaitingEmail.getOrDefault(chatId, false)) {
      if (!isValidEmail(messageText)) {
        messageService.sendMessage(chatId, "Invalid email format. Please try again.");
        return;
      }

      final String code = emailService.generateCode();

      repository.findByTelegramId(telegramId)
          .flatMap(profile -> {
            profile.setEmail(messageText);
            profile.setEmailVerified(false);
            profile.setEmailVerificationCode(code);
            profile.setEmailVerificationCreatedAt(Instant.now());

            return repository.save(profile)
                .then(emailService.sendVerificationEmail(messageText, code))
                .thenReturn(profile);
          })
          .doOnSuccess(profile -> {
            messageService.sendMessage(chatId, "A verification code has been sent to your email.");
            awaitingEmail.remove(chatId);
          })
          .doOnError(error -> {
            log.error("Error processing email for user {}: {}", telegramId, error.getMessage());
            messageService.sendMessage(chatId, "Failed to send verification code. Please try again later.");
            awaitingEmail.remove(chatId);
          })
          .subscribe();
    }
  }

  @Override
  public String getCommand() {
    return BotCommandType.EMAIL.getCommand();
  }

  @Override
  public boolean shouldHandle(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final String text = update.getMessage().getText();

    if (text.equalsIgnoreCase(getCommand())) return true;
    return awaitingEmail.getOrDefault(chatId, false);
  }

  private boolean isValidEmail(String email) {
    return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
  }
}

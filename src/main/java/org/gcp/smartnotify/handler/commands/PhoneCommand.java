package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.repo.UserProfileRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@AllArgsConstructor
public class PhoneCommand implements org.gcp.smartnotify.handler.Command {

  private final MessageService messageService;
  private final UserProfileRepository repository;

  private final Map<String, Boolean> awaitingPhoneNumber = new ConcurrentHashMap<>();

  @Override
  public void execute(Update update) {
    String chatId = extractChatId(update);
    String text = extractText(update);

    if (isPhoneCommand(text)) {
      promptPhoneNumber(chatId);
    } else if (isAwaitingPhone(chatId)) {
      handlePhoneNumberInput(chatId, text);
    }
  }

  @Override
  public boolean shouldHandle(Update update) {
    String chatId = extractChatId(update);
    String text = extractText(update);

    return isPhoneCommand(text) || isAwaitingPhone(chatId);
  }

  @Override
  public String getCommand() {
    return BotCommandType.PHONE.getCommand();
  }

  private boolean isPhoneCommand(String text) {
    return BotCommandType.PHONE.getCommand().equalsIgnoreCase(text);
  }

  private boolean isAwaitingPhone(String chatId) {
    return awaitingPhoneNumber.getOrDefault(chatId, false);
  }

  private void promptPhoneNumber(String chatId) {
    messageService.sendMessage(chatId, BotCommandType.PHONE.getMessage());
    awaitingPhoneNumber.put(chatId, true);
  }

  private void handlePhoneNumberInput(String chatId, String phone) {
    if (!isValidPhoneNumber(phone)) {
      messageService.sendMessage(chatId, "Invalid phone number. Please try again.");
      return;
    }

    Long telegramId = parseTelegramId(chatId);
    if (telegramId == null) {
      messageService.sendMessage(chatId, "Error: Invalid Telegram ID format.");
      awaitingPhoneNumber.remove(chatId);
      return;
    }

    repository.findByTelegramId(telegramId)
        .flatMap(user -> {
          user.setPhone(phone);
          return repository.save(user);
        })
        .doOnSuccess(savedUser -> {
          log.info("User updated: {}", savedUser);
          messageService.sendMessage(chatId, "You have successfully added your phone!");
          awaitingPhoneNumber.remove(chatId);
        })
        .switchIfEmpty(Mono.fromRunnable(() -> {
          messageService.sendMessage(chatId, "User not found in the database.");
          awaitingPhoneNumber.remove(chatId);
        }))
        .subscribe();
  }

  private Long parseTelegramId(String chatId) {
    try {
      return Long.parseLong(chatId);
    } catch (NumberFormatException e) {
      log.warn("Failed to parse Telegram ID: {}", chatId, e);
      return null;
    }
  }

  private boolean isValidPhoneNumber(String phone) {
    return phone != null && phone.matches("\\+?[0-9]{10,15}");
  }

  private String extractChatId(Update update) {
    return update.getMessage().getChatId().toString();
  }

  private String extractText(Update update) {
    return update.getMessage().getText();
  }
}

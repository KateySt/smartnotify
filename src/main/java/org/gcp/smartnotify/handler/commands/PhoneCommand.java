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
public class PhoneCommand implements org.gcp.smartnotify.handler.commands.Command {

  private final MessageService messageService;
  private final UserProfileRepository repository;

  private final Map<String, Boolean> awaitingPhoneNumber = new ConcurrentHashMap<>();

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final String text = update.getMessage().getText();

    if (text.equalsIgnoreCase(BotCommandType.PHONE.getCommand())) {
      messageService.sendMessage(chatId, BotCommandType.PHONE.getMessage());
      awaitingPhoneNumber.put(chatId, true);
    } else if (awaitingPhoneNumber.getOrDefault(chatId, false)) {
      if (isValidPhoneNumber(text)) {
        Long telegramId;

        try {
          telegramId = Long.parseLong(chatId);
        } catch (NumberFormatException e) {
          messageService.sendMessage(chatId, "Error: Invalid Telegram ID format.");
          awaitingPhoneNumber.remove(chatId);
          return;
        }
        log.info("{}", telegramId);
        repository.findByTelegramId(telegramId)
            .flatMap(user -> {
              user.setPhone(text);
              return repository.save(user);
            })
            .doOnSuccess(savedUser -> {
              log.info("--->{}", savedUser);
              messageService.sendMessage(chatId, "You have successfully add phone!");
              awaitingPhoneNumber.remove(chatId);
            })
            .switchIfEmpty(Mono.fromRunnable(() -> {
              messageService.sendMessage(chatId, "User not found in the database.");
              awaitingPhoneNumber.remove(chatId);
            }))
            .subscribe();

      } else {
        messageService.sendMessage(chatId, "Invalid phone number. Please try again.");
      }
    }
  }

  @Override
  public boolean shouldHandle(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final String text = update.getMessage().getText();

    if (text.equalsIgnoreCase(getCommand())) return true;
    return awaitingPhoneNumber.getOrDefault(chatId, false);
  }

  @Override
  public String getCommand() {
    return BotCommandType.PHONE.getCommand();
  }

  private boolean isValidPhoneNumber(String phone) {
    return phone != null && phone.matches("\\+?[0-9]{10,15}");
  }
}
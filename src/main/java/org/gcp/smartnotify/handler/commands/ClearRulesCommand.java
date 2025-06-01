package org.gcp.smartnotify.handler.commands;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ClearRulesCommand implements org.gcp.smartnotify.handler.Command {

  private final MessageService messageService;
  private final NotificationRuleRepository ruleRepository;

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    final Long telegramId = update.getMessage().getFrom().getId();

    ruleRepository.findByTelegramId(telegramId)
        .flatMap(ruleRepository::delete)
        .doOnSuccess(deletedRules ->
            messageService.sendMessage(chatId, "All your rules have been successfully deleted!"))
        .doOnError(e -> {
          messageService.sendMessage(chatId, "Error deleting rules: " + e.getMessage());
        })
        .subscribe();
  }

  @Override
  public String getCommand() {
    return BotCommandType.DELETE_RULES.getCommand();
  }
}

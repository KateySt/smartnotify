package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class ListRulesCommand implements org.gcp.smartnotify.handler.Command {

  private final NotificationRuleRepository ruleRepository;
  private final MessageService messageService;

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();

    ruleRepository.findAllByTelegramId(Long.valueOf(chatId))
        .collectList()
        .doOnSuccess(rules -> {
          if (rules.isEmpty()) {
            messageService.sendMessage(chatId, "You don't have any notification rules yet.");
            return;
          }

          StringBuilder sb = new StringBuilder();
          sb.append("📋 Your Notification Rules:\n\n");
          for (int i = 0; i < rules.size(); i++) {
            NotificationRule rule = rules.get(i);
            sb.append("🔸 Rule #").append(i + 1).append("\n");
            sb.append("🕓 Scheduled At: ").append(rule.getScheduledAt()).append("\n");
            sb.append("📢 Channels: ").append(rule.getChannel()).append("\n");
            sb.append("💬 Message: ").append(rule.getMessage()).append("\n");
            sb.append("✅ Active: ").append(rule.isActive()).append("\n\n");
          }

          messageService.sendMessage(chatId, sb.toString());
        })
        .doOnError(e -> {
          log.error("Failed to fetch rules", e);
          messageService.sendMessage(chatId, "An error occurred while fetching your rules.");
        })
        .subscribe();
  }

  @Override
  public boolean shouldHandle(Update update) {
    return update.getMessage().getText().equalsIgnoreCase(BotCommandType.LIST_RULES.getCommand());
  }

  @Override
  public String getCommand() {
    return BotCommandType.LIST_RULES.getCommand();
  }
}

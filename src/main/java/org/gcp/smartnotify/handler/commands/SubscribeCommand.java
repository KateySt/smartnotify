package org.gcp.smartnotify.handler.commands;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscribeCommand implements org.gcp.smartnotify.handler.Command {

  private final MessageService messageService;
  private final NotificationRuleRepository notificationRuleRepository;

  @Override
  public void execute(Update update) {
    Long telegramId = update.getMessage().getFrom().getId();
    String chatId = update.getMessage().getChatId().toString();

    notificationRuleRepository.findByTelegramId(telegramId)
        .defaultIfEmpty(createDefaultRule(telegramId))
        .flatMap(rule -> {
          SendMessage message = SendMessage.builder()
              .chatId(chatId)
              .text("Choose your notification channels:")
              .replyMarkup(buildChannelKeyboard(rule))
              .build();

          messageService.sendMessage(message);
          return Mono.empty();
        })
        .subscribe();
  }

  @Override
  public String getCommand() {
    return BotCommandType.SUBSCRIBE.getCommand();
  }

  private NotificationRule createDefaultRule(Long telegramId) {
    return NotificationRule.builder()
        .telegramId(telegramId)
        .channel(new ArrayList<>())
        .active(true)
        .build();
  }

  private InlineKeyboardMarkup buildChannelKeyboard(NotificationRule rule) {
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Channel channel : Channel.values()) {
      boolean selected = rule.getChannel() != null && rule.getChannel().contains(channel);

      InlineKeyboardButton button = InlineKeyboardButton.builder()
          .text((selected ? "✅ " : "") + channel.name())
          .callbackData("toggle_channel_" + channel.name())
          .build();

      rows.add(List.of(button));
    }

    return InlineKeyboardMarkup.builder()
        .keyboard(rows)
        .build();
  }
}

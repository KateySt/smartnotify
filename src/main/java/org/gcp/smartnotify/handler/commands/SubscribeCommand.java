package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class SubscribeCommand implements org.gcp.smartnotify.handler.Command {

  private final MessageService messageService;
  private final NotificationRuleRepository notificationRule;

  @Override
  public void execute(Update update) {
    final Long telegramId = update.getMessage().getFrom().getId();
    final String chatId = update.getMessage().getChatId().toString();

    notificationRule.findByTelegramId(telegramId)
        .defaultIfEmpty(NotificationRule.builder()
            .telegramId(telegramId)
            .channel(new ArrayList<>())
            .active(true)
            .build())
        .flatMap(rule -> {
          final SendMessage message = new SendMessage();
          message.setChatId(chatId);
          message.setText("Choose your notification channels:");

          final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
          final List<List<InlineKeyboardButton>> rows = new ArrayList<>();

          for (Channel ch : Channel.values()) {
            final boolean selected = rule.getChannel() != null && rule.getChannel().contains(ch);
            final InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText((selected ? "✅ " : "") + ch.name());
            button.setCallbackData("toggle_channel_" + ch.name());
            rows.add(List.of(button));
          }

          markup.setKeyboard(rows);
          message.setReplyMarkup(markup);

          messageService.sendMessage(message);
          return Mono.empty();
        })
        .subscribe();
  }

  @Override
  public String getCommand() {
    return BotCommandType.SUBSCRIBE.getCommand();
  }
}
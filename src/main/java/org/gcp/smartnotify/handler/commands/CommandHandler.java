package org.gcp.smartnotify.handler.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {
  private final MessageService messageService;
  private final NotificationRuleRepository notificationRule;
  private final List<org.gcp.smartnotify.handler.Command> commands;

  public void handleUpdate(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      final String messageText = update.getMessage().getText().trim();
      final String commandText = messageText.split(" ")[0].toLowerCase();

      if (!BotCommandType.isSupported(commandText)) {
        commands.stream()
            .filter(command -> command.shouldHandle(update))
            .findFirst()
            .ifPresent(command -> command.execute(update));
        return;
      }

      commands.stream()
          .filter(command -> command.getCommand().equalsIgnoreCase(commandText))
          .findFirst()
          .ifPresent(command -> command.execute(update));
    } else if (update.hasCallbackQuery()) {
      handleCallback(update.getCallbackQuery());
    }
  }

  private void handleCallback(CallbackQuery callbackQuery) {
    final String data = callbackQuery.getData();
    final Long telegramId = callbackQuery.getFrom().getId();
    final String chatId = callbackQuery.getMessage().getChatId().toString();

    if (data.startsWith("toggle_channel_")) {
      final String channelStr = data.replace("toggle_channel_", "");
      final Channel channel = Channel.valueOf(channelStr);
      updateChannelSubscriptionTemp(telegramId, channel, chatId);
    }
  }

  private void updateChannelSubscriptionTemp(Long telegramId, Channel channel, String chatId) {
    notificationRule.findByTelegramId(telegramId)
        .defaultIfEmpty(NotificationRule.builder()
            .telegramId(telegramId)
            .channel(new ArrayList<>())
            .active(true)
            .build())
        .flatMap(rule -> {
          final List<Channel> channels = rule.getChannel();
          if (channels.contains(channel)) {
            channels.remove(channel);
          } else {
            channels.add(channel);
          }
          rule.setChannel(channels);
          return notificationRule.save(rule);
        })
        .flatMap(savedRule -> {
          final SendMessage message = new SendMessage();
          message.setChatId(chatId);
          message.setText("Choose channel:");

          final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
          final List<List<InlineKeyboardButton>> rows = new ArrayList<>();

          for (Channel ch : Channel.values()) {
            final boolean selected = savedRule.getChannel().contains(ch);
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
}

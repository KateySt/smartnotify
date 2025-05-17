package org.gcp.smartnotify.component;

import org.gcp.smartnotify.model.SmartNotifyBot;
import org.gcp.smartnotify.service.impl.BotSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class TelegramBotSender implements BotSender {

  private final SmartNotifyBot bot;

  public TelegramBotSender(@Lazy SmartNotifyBot bot) {
    this.bot = bot;
  }

  @Override
  public void send(SendMessage message) {
    try {
      bot.execute(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void send(String chatId, String text) {

    try {
      bot.execute(SendMessage.builder()
          .chatId(chatId)
          .text(text)
          .build());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

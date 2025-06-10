package org.gcp.smartnotify.component;

import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.model.SmartNotifyBot;
import org.gcp.smartnotify.service.BotSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

@Slf4j
@Component
public class TelegramBotSender implements BotSender {

  private final SmartNotifyBot bot;

  public TelegramBotSender(@Lazy SmartNotifyBot bot) {
    this.bot = bot;
  }


  private <T> T execute(SendMessage message, Function<SendMessage, T> executor) {
    return executor.apply(message);
  }

  @Override
  public void send(SendMessage message) {
    execute(message, msg -> {
      try {
        bot.execute(msg);
      } catch (TelegramApiException e) {
        throw new RuntimeException(e);
      }
      return null;
    });
  }

  @Override
  public void send(String chatId, String text) {
    SendMessage msg = SendMessage.builder().chatId(chatId).text(text).build();
    send(msg);
  }
}

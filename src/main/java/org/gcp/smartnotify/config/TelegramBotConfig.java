package org.gcp.smartnotify.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.model.SmartNotifyBot;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig {

  private final SmartNotifyBot smartNotifyBot;

  @PostConstruct
  public void registerBot() {
    try {
      final TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
      botsApi.registerBot(smartNotifyBot);
      log.info("Bot successfully registered.");
    } catch (TelegramApiException e) {
      e.printStackTrace();
      log.error("Failed to register bot.");
    }
  }
}
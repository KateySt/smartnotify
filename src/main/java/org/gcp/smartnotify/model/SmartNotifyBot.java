package org.gcp.smartnotify.model;

import org.gcp.smartnotify.handler.commands.CommandHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SmartNotifyBot extends TelegramLongPollingBot {

  private final CommandHandler commandHandler;
  private final String botUsername;

  public SmartNotifyBot(CommandHandler commandHandler,
                        @Value("${telegram.bot.token}") String botToken,
                        @Value("${telegram.bot.username}") String botUsername) {
    super(botToken);
    this.commandHandler = commandHandler;
    this.botUsername = botUsername;
  }

  @Override
  public void onUpdateReceived(Update update) {
    commandHandler.handleUpdate(update);
  }

  @Override
  public String getBotUsername() {
    return botUsername;
  }
}

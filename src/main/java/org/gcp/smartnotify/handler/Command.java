package org.gcp.smartnotify.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
  void execute(Update update);

  String getCommand();

  default boolean shouldHandle(Update update) {
    final String messageText = update.getMessage().getText();
    return messageText.equalsIgnoreCase(getCommand());
  }
}
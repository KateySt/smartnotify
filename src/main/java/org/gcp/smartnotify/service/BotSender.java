package org.gcp.smartnotify.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface BotSender {
  void send(String chatId, String text);

  void send(SendMessage message);
}

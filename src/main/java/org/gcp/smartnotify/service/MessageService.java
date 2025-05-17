package org.gcp.smartnotify.service;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.service.impl.BotSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final BotSender botSender;

  public void sendMessage(SendMessage message) {
    botSender.send(message);
  }

  public void sendMessage(String chatId, String text) {
    botSender.send(chatId, text);
  }
}
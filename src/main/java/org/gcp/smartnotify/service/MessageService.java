package org.gcp.smartnotify.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.io.File;

public interface MessageService {
  void sendMessage(SendMessage message);

  void sendMessage(String chatId, String text);

  Mono<File> downloadFile(String fileId);
}

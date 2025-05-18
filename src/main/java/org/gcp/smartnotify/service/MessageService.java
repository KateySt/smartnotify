package org.gcp.smartnotify.service;

import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.model.SmartNotifyBot;
import org.gcp.smartnotify.service.impl.BotSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final BotSender botSender;
  private final ObjectProvider<SmartNotifyBot> botProvider;

  public void sendMessage(SendMessage message) {
    botSender.send(message);
  }

  public void sendMessage(String chatId, String text) {
    botSender.send(chatId, text);
  }

  public Mono<File> downloadFile(String fileId) {
    return Mono.fromCallable(() -> {
      final SmartNotifyBot bot = botProvider.getObject();
      final org.telegram.telegrambots.meta.api.objects.File telegramFile = bot.execute(new GetFile(fileId));
      return bot.downloadFile(telegramFile);
    }).subscribeOn(Schedulers.boundedElastic());
  }
}

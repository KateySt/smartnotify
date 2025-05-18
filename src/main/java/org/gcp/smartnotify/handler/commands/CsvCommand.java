package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.enums.Channel;
import org.gcp.smartnotify.model.entity.NotificationRule;
import org.gcp.smartnotify.repo.NotificationRuleRepository;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class CsvCommand implements org.gcp.smartnotify.handler.commands.Command {

  private final MessageService messageService;
  private final NotificationRuleRepository ruleRepository;

  private final Map<String, Boolean> awaitingCsvUpload = new ConcurrentHashMap<>();

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();

    if (update.getMessage().getText().equalsIgnoreCase(BotCommandType.UPLOAD_CSV.getCommand())) {
      messageService.sendMessage(chatId, "Please upload your CSV file with notification rules.");
      awaitingCsvUpload.put(chatId, true);
      return;
    }

    if (awaitingCsvUpload.getOrDefault(chatId, false)) {
      if (update.getMessage().hasDocument()) {
        var document = update.getMessage().getDocument();
        if (!document.getFileName().endsWith(".csv")) {
          messageService.sendMessage(chatId, "Please upload a valid CSV file.");
          return;
        }

        parseAndSaveCsv(document, chatId);
      } else {
        messageService.sendMessage(chatId, "Please upload a CSV file.");
      }
    }
  }

  @Override
  public boolean shouldHandle(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    return (update.getMessage().getText() != null && update.getMessage().getText().equalsIgnoreCase("/uploadcsv"))
        || awaitingCsvUpload.getOrDefault(chatId, false);
  }

  @Override
  public String getCommand() {
    return BotCommandType.UPLOAD_CSV.getCommand();
  }

  private void parseAndSaveCsv(Document document, String chatId) {
    String fileId = document.getFileId();

    messageService.downloadFile(fileId)
        .flatMapMany(file -> parseCsvLines(file))
        .flatMap(this::mapToNotificationRule)
        .flatMap(ruleRepository::save)
        .collectList()
        .doOnSuccess(savedRules -> {
          messageService.sendMessage(chatId, "CSV processed successfully! Added " + savedRules.size() + " rules.");
          awaitingCsvUpload.remove(chatId);
        })
        .doOnError(e -> {
          log.error("Error processing CSV", e);
          messageService.sendMessage(chatId, "Error processing CSV: " + e.getMessage());
          awaitingCsvUpload.remove(chatId);
        })
        .subscribe();
  }

  private Flux<String[]> parseCsvLines(File file) {
    return Mono.fromCallable(() -> new BufferedReader(new FileReader(file)))
        .flatMapMany(reader -> Flux.fromStream(reader.lines())
            .skip(1)
            .map(line -> line.split(","))
            .publishOn(Schedulers.boundedElastic())
            .doFinally(signalType -> {
              try {
                reader.close();
              } catch (IOException e) {
                log.error("{}", e.toString());
              }
            })
        );
  }


  private Mono<NotificationRule> mapToNotificationRule(String[] columns) {
    try {
      Long telegramId = Long.parseLong(columns[0].trim());
      Instant scheduledAt = Instant.parse(columns[1].trim());
      List<Channel> channels = Arrays.stream(columns[2].split(";"))
          .map(String::trim)
          .map(Channel::valueOf)
          .collect(Collectors.toList());
      String message = columns[3].trim();
      boolean active = Boolean.parseBoolean(columns[4].trim());

      NotificationRule rule = NotificationRule.builder()
          .telegramId(telegramId)
          .scheduledAt(scheduledAt)
          .channel(channels)
          .message(message)
          .active(active)
          .build();

      return Mono.just(rule);
    } catch (Exception e) {
      return Mono.error(new IllegalArgumentException("Invalid CSV format: " + Arrays.toString(columns)));
    }
  }
}

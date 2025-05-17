package org.gcp.smartnotify.handler.commands;

import lombok.AllArgsConstructor;
import org.gcp.smartnotify.enums.BotCommandType;
import org.gcp.smartnotify.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class HelpCommand implements org.gcp.smartnotify.handler.commands.Command {

  private final MessageService messageService;

  @Override
  public void execute(Update update) {
    final String chatId = update.getMessage().getChatId().toString();
    messageService.sendMessage(chatId, BotCommandType.listCommands());
  }

  @Override
  public String getCommand() {
    return BotCommandType.HELP.getCommand();
  }
}
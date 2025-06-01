package org.gcp.smartnotify.enums;

import lombok.Getter;

@Getter
public enum BotCommandType {
  DELETE_RULES("/clearRules", "", ""),
  LIST_RULES("/listRules", "Get list of rules", ""),
  START("/start", "Start interacting with the bot", "Welcome to MindPoke!"),
  HELP("/help", "Get help about available commands", ""),
  SUBSCRIBE("/subscribe", "Subscribe to notifications", "You have successfully subscribed to notifications!"),
  EMAIL("/email", "Add email for notification", "You have successfully subscribed to mail notifications!"),
  EMAIL_VERIFY("/verify", "Verify your email. Please enter the verification code like this: /verify 123ABC", "Email verified successfully!"),
  PHONE("/phone", "Add phone number for notification", "Please enter your phone number:"),
  UPLOAD_CSV("/uploadCsv", "Upload CSV with rules", "Please send the .csv file now.");

  private final String command;
  private final String description;
  private final String message;

  BotCommandType(String command, String description, String message) {
    this.command = command;
    this.description = description;
    this.message = message;
  }

  public static boolean isSupported(String command) {
    for (BotCommandType type : values()) {
      if (type.getCommand().equalsIgnoreCase(command)) {
        return true;
      }
    }
    return false;
  }

  public static String listCommands() {
    final StringBuilder builder = new StringBuilder("Available commands:\n\n");
    for (BotCommandType type : values()) {
      builder.append(type.getCommand())
          .append(" — ")
          .append(type.getDescription())
          .append("\n");
    }
    return builder.toString();
  }
}
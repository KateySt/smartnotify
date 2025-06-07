package org.gcp.smartnotify.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.model.dto.NotificationEvent;
import org.gcp.smartnotify.service.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailNotification implements NotificationSender {
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public void send(NotificationEvent event) {
    try {
      final MimeMessage message = mailSender.createMimeMessage();
      final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      final Context context = new Context();
      context.setVariables(event.getVariables());

      final String htmlContent = templateEngine.process(event.getTemplateName(), context);

      helper.setTo(event.getTo());
      helper.setSubject(event.getSubject());
      helper.setText(htmlContent, true);
      helper.setFrom(fromEmail);

      mailSender.send(message);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
package org.gcp.smartnotify.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.gcp.smartnotify.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public Mono<Void> sendVerificationEmail(String to, String code) {
    return Mono.fromRunnable(() -> {
      try {
        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true);

        final Context context = new Context();
        context.setVariable("code", code);
        final String html = templateEngine.process("verify-email", context);

        helper.setTo(to);
        helper.setSubject("Verify your email");
        helper.setText(html, true);
        helper.setFrom(fromEmail);

        mailSender.send(message);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }).subscribeOn(Schedulers.boundedElastic()).then();
  }

  public String generateCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }
}
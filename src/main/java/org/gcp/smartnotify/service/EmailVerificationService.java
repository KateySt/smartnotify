package org.gcp.smartnotify.service;

import reactor.core.publisher.Mono;

public interface EmailVerificationService {

  Mono<Void> sendVerificationEmail(String to, String code);

  String generateCode();
}

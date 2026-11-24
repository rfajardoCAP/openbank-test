package com.capgemini.test.code.service.notification;

import com.capgemini.test.code.model.NotificationChannel;
import com.capgemini.test.code.model.User;

/**
 * Estrategia de notificación: cada implementación envía mensajes por un único canal. Añadir un
 * canal consiste en añadir una implementación, sin tocar {@code UserService}.
 */
public interface NotificationSender {

  NotificationChannel channel();

  /**
   * @throws com.capgemini.test.code.exception.ExternalServiceException si el envío falla
   */
  void send(User recipient, String message);
}

package com.capgemini.test.code.service.notification;

import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.NotificationClient.EmailRequest;
import com.capgemini.test.code.exception.ExternalServiceException;
import com.capgemini.test.code.model.NotificationChannel;
import com.capgemini.test.code.model.User;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

  private static final String SERVICE_NAME = "email";

  private final NotificationClient notificationClient;

  public EmailNotificationSender(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void send(User recipient, String message) {
    try {
      notificationClient.sendEmail(new EmailRequest(recipient.getEmail(), message));
    } catch (FeignException failure) {
      throw new ExternalServiceException(SERVICE_NAME, failure);
    }
  }
}

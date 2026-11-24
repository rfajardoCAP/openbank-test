package com.capgemini.test.code.service.notification;

import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.NotificationClient.SmsRequest;
import com.capgemini.test.code.exception.ExternalServiceException;
import com.capgemini.test.code.model.NotificationChannel;
import com.capgemini.test.code.model.User;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {

  private static final String SERVICE_NAME = "sms";

  private final NotificationClient notificationClient;

  public SmsNotificationSender(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.SMS;
  }

  @Override
  public void send(User recipient, String message) {
    try {
      notificationClient.sendSms(new SmsRequest(recipient.getPhone(), message));
    } catch (FeignException failure) {
      throw new ExternalServiceException(SERVICE_NAME, failure);
    }
  }
}

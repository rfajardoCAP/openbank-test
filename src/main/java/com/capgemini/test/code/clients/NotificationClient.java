package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente de la API de notificaciones: {@code POST /email} y {@code POST /sms}.
 */
@FeignClient(name = "notificationClient", url = "${external.service.url}", configuration = ExternalServiceFeignConfig.class)
public interface NotificationClient {

  @PostMapping("/email")
  void sendEmail(@RequestBody EmailRequest request);

  @PostMapping("/sms")
  void sendSms(@RequestBody SmsRequest request);

  record EmailRequest(String email, String message) {
  }

  record SmsRequest(String phone, String message) {
  }
}

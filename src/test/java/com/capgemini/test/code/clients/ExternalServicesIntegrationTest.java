package com.capgemini.test.code.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import com.capgemini.test.code.service.DniValidationService;
import com.capgemini.test.code.service.notification.EmailNotificationSender;
import com.capgemini.test.code.service.notification.SmsNotificationSender;
import com.capgemini.test.code.support.ApplicationIntegrationTest;
import com.capgemini.test.code.support.TestMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integración de los clientes Feign con la API externa real (MockServer con los stubs del
 * enunciado): serialización de las peticiones, rutas, métodos y traducción de las respuestas.
 */
class ExternalServicesIntegrationTest extends ApplicationIntegrationTest {

  private static final User ADMIN = User.builder().id(1L).name("pablo").email("email@email.com")
      .phone("677998899").role(Role.ADMIN).dni("23454234F").roomId(1L).build();

  @Autowired
  private DniValidationService dniValidationService;
  @Autowired
  private EmailNotificationSender emailSender;
  @Autowired
  private SmsNotificationSender smsSender;

  @Test
  void acceptsAnyDniExceptTheOneTheServiceRejects() {
    assertThat(dniValidationService.isValid("23454234F")).isTrue();
    assertThat(dniValidationService.isValid("99999999w")).isFalse();
  }

  @Test
  void sendsTheDniAsJson() {
    dniValidationService.isValid("23454234F");

    assertThat(TestMockServer.receivedOnce("""
        {"method":"PATCH","path":"/check-dni","body":{"type":"JSON","json":{"dni":"23454234F"}}}"""))
        .isTrue();
  }

  @Test
  void sendsTheEmailNotification() {
    emailSender.send(ADMIN, "usuario guardado");

    assertThat(TestMockServer.receivedOnce("""
        {"method":"POST","path":"/email",
         "body":{"type":"JSON","json":{"email":"email@email.com","message":"usuario guardado"}}}"""))
        .isTrue();
  }

  @Test
  void sendsTheSmsNotification() {
    smsSender.send(ADMIN, "usuario guardado");

    assertThat(TestMockServer.receivedOnce("""
        {"method":"POST","path":"/sms",
         "body":{"type":"JSON","json":{"phone":"677998899","message":"usuario guardado"}}}"""))
        .isTrue();
  }
}

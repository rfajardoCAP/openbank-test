package com.capgemini.test.code.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.support.ApplicationIntegrationTest;
import com.capgemini.test.code.support.TestMockServer;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tests de aceptación: los requisitos del README comprobados desde fuera, por HTTP, contra la
 * aplicación completa con PostgreSQL y la API externa reales.
 */
@DisplayName("Gestión de usuarios de la sala 1")
class UserAcceptanceTest extends ApplicationIntegrationTest {

  @Autowired
  private TestRestTemplate http;
  @Autowired
  private JdbcTemplate jdbc;

  /** Deja la base de datos solo con los datos iniciales de la migración. */
  @AfterEach
  void deleteCreatedUsers() {
    jdbc.update("DELETE FROM users WHERE email NOT IN ('juan@example.com', 'maria@example.com')");
  }

  @Nested
  @DisplayName("Crear usuario")
  class CreateUser {

    @Test
    @DisplayName("guarda el usuario en la sala 1 y devuelve 201 con su id")
    void createsTheUser() {
      ResponseEntity<Map> response = create(user("pablo", "email@email.com", "677998899", "admin", "23454234F"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      String id = (String) response.getBody().get("id");
      assertThat(id).isNotBlank();
      assertThat(jdbc.queryForObject("SELECT room_id FROM users WHERE id = ?", Long.class, Long.valueOf(id)))
          .isEqualTo(1L);
    }

    @Test
    @DisplayName("notifica al admin por email con el mensaje 'usuario guardado'")
    void notifiesAdminByEmail() {
      create(user("pablo", "email@email.com", "677998899", "admin", "23454234F"));

      assertThat(TestMockServer.receivedOnce("""
          {"method":"POST","path":"/email",
           "body":{"type":"JSON","json":{"email":"email@email.com","message":"usuario guardado"}}}"""))
          .isTrue();
      assertThat(TestMockServer.receivedOnce("""
          {"method":"POST","path":"/sms"}""")).isFalse();
    }

    @Test
    @DisplayName("notifica al superadmin por SMS con el mensaje 'usuario guardado'")
    void notifiesSuperadminBySms() {
      create(user("ana", "ana@email.com", "655443322", "superadmin", "23454234F"));

      assertThat(TestMockServer.receivedOnce("""
          {"method":"POST","path":"/sms",
           "body":{"type":"JSON","json":{"phone":"655443322","message":"usuario guardado"}}}"""))
          .isTrue();
    }

    @Test
    @DisplayName("rechaza un nombre de más de 6 caracteres")
    void rejectsLongName() {
      assertConflict(create(user("pablito", "email@email.com", "677998899", "admin", "23454234F")), "userName");
    }

    @Test
    @DisplayName("rechaza un email sin @ o sin punto")
    void rejectsInvalidEmail() {
      assertConflict(create(user("pablo", "email.com", "677998899", "admin", "23454234F")), "email");
    }

    @Test
    @DisplayName("rechaza un rol que no sea admin ni superadmin")
    void rejectsInvalidRole() {
      assertConflict(create(user("pablo", "email@email.com", "677998899", "user", "23454234F")), "rol");
    }

    @Test
    @DisplayName("rechaza un email que ya existe")
    void rejectsDuplicatedEmail() {
      assertConflict(create(user("pablo", "juan@example.com", "677998899", "admin", "23454234F")), "email");
    }

    @Test
    @DisplayName("rechaza un DNI con la letra de control incorrecta sin consultar la API externa")
    void rejectsDniWithWrongControlLetter() {
      // Es el DNI del ejemplo del enunciado: a 23454234 le corresponde la letra F.
      assertConflict(create(user("pablo", "email@email.com", "677998899", "admin", "23454234W")), "dni");

      assertThat(TestMockServer.receivedOnce("""
          {"method":"PATCH","path":"/check-dni"}""")).isFalse();
    }

    @Test
    @DisplayName("rechaza un DNI real que la API externa no valida")
    void rejectsDniRefusedByTheExternalApi() {
      // La API del enunciado solo rechaza 99999999w, que ya no pasa la validación local; se simula
      // el rechazo de un DNI bien formado.
      TestMockServer.addExpectation("""
          {"priority": 10,
           "httpRequest": {"method": "PATCH", "path": "/check-dni",
                           "body": {"type": "JSON", "json": {"dni": "99999999R"}}},
           "httpResponse": {"statusCode": 409, "body": "{\\"message\\": \\"Invalid DNI\\"}"}}""");

      assertConflict(create(user("pablo", "email@email.com", "677998899", "admin", "99999999R")), "dni");

      assertThat(TestMockServer.receivedOnce("""
          {"method":"PATCH","path":"/check-dni"}""")).isTrue();
    }

    @Test
    @DisplayName("si la notificación falla no guarda el usuario y devuelve 503")
    void rollsBackWhenTheNotificationFails() {
      TestMockServer.addExpectation("""
          {"priority": 10,
           "httpRequest": {"method": "POST", "path": "/email"},
           "httpResponse": {"statusCode": 500}}""");

      ResponseEntity<Map> response = create(user("pablo", "email@email.com", "677998899", "admin", "23454234F"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
      assertThat(response.getBody()).containsEntry("code", 503);
      assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE email = ?", Long.class, "email@email.com"))
          .isZero();
    }
  }

  @Nested
  @DisplayName("Obtener usuario")
  class GetUser {

    @Test
    @DisplayName("devuelve el usuario creado")
    void returnsTheCreatedUser() {
      String id = (String) create(user("pablo", "email@email.com", "677998899", "superadmin", "23454234F"))
          .getBody().get("id");

      ResponseEntity<Map> response = http.getForEntity("/users/" + id, Map.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsAllEntriesOf(Map.of(
          "id", id, "name", "pablo", "email", "email@email.com", "phone", "677998899",
          "rol", "superadmin", "dni", "23454234F"));
    }
  }

  private ResponseEntity<Map> create(Map<String, String> body) {
    return http.postForEntity("/users", body, Map.class);
  }

  private static Map<String, String> user(String name, String email, String phone, String rol, String dni) {
    return Map.of("name", name, "email", email, "phone", phone, "rol", rol, "dni", dni);
  }

  private static void assertConflict(ResponseEntity<Map> response, String field) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody())
        .containsEntry("code", 409)
        .containsEntry("message", "error validation " + field);
  }
}

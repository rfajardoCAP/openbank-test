package com.capgemini.test.code.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import com.capgemini.test.code.support.TestDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integración del repositorio con las migraciones de Flyway, incluidos los datos iniciales de
 * {@code V1__init.sql}, sobre H2 en modo PostgreSQL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

  private static final long ROOM_1 = 1L;

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    TestDatabase.registerProperties(registry);
  }

  @Autowired
  private UserRepository repository;
  @Autowired
  private TestEntityManager entityManager;

  @Test
  void savesTheUserAndAssignsAnId() {
    User saved = repository.saveAndFlush(newUser("pablo@email.com", "23454234F", "677998899"));

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findByIdAndRoomId(saved.getId(), ROOM_1)).get()
        .extracting(User::getEmail, User::getRole)
        .containsExactly("pablo@email.com", Role.ADMIN);
  }

  @Test
  void readsTheSeededUserOfRoomOneEvenIfItBreaksTheCurrentRules() {
    assertThat(repository.findByIdAndRoomId(1L, ROOM_1)).get()
        .extracting(User::getName, User::getRole)
        .containsExactly("Juan Pérez", Role.SUPERADMIN);
  }

  @Test
  void readsTheSeededUserOfRoomTwo() {
    assertThat(repository.findById(2L)).get()
        .extracting(User::getName, User::getRole)
        .containsExactly("María López", Role.ADMIN);
  }

  @Test
  void storesTheRoleInLowercase() {
    User saved = repository.saveAndFlush(User.builder().name("ana").email("ana@email.com").phone("655443322")
        .role(Role.SUPERADMIN).dni("23454234F").roomId(ROOM_1).build());

    Object column = entityManager.getEntityManager()
        .createNativeQuery("SELECT role FROM users WHERE id = :id")
        .setParameter("id", saved.getId())
        .getSingleResult();
    assertThat(column).isEqualTo("superadmin");
  }

  @Test
  void doesNotFindUsersOfAnotherRoom() {
    assertThat(repository.findByIdAndRoomId(2L, ROOM_1)).isEmpty();
  }

  @Test
  void knowsWhetherAnEmailIsTaken() {
    assertThat(repository.existsByEmail("juan@example.com")).isTrue();
    assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
  }

  /**
   * Email, DNI y teléfono son únicos. No se comprueba el nombre de la restricción: depende del motor
   * y H2 no los llama como PostgreSQL ({@code users_email_key}...).
   */
  @ParameterizedTest
  @CsvSource({
      "juan@example.com, 11111111H, 0034666666666",
      "other@email.com,  12345678Z, 0034666666666",
      "juan@example.com,  11111111H, 600000001"})
  void rejectsDuplicatedEmailDniOrPhone(String email, String dni, String phone) {
    assertThatThrownBy(() -> repository.saveAndFlush(newUser(email, dni, phone)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private static User newUser(String email, String dni, String phone) {
    return User.builder().name("pablo").email(email).phone(phone).role(Role.ADMIN).dni(dni).roomId(ROOM_1).build();
  }
}

package com.capgemini.test.code.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import com.capgemini.test.code.model.Role;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

  private final UserValidator validator = new UserValidator();

  @Test
  void acceptsAValidRequestAndReturnsItsRole() {
    assertThat(validator.validate(request("pablo", "email@email.com", "677998899", "superadmin", "23454234F")))
        .isEqualTo(Role.SUPERADMIN);
  }

  @Test
  void acceptsNameOfExactlySixCharacters() {
    assertThat(validator.validate(request("123456", "email@email.com", "677998899", "admin", "23454234F")))
        .isEqualTo(Role.ADMIN);
  }

  @Test
  void rejectsNameLongerThanSixCharacters() {
    assertError(() -> validator.validate(request("pablito", "email@email.com", "677998899", "admin", "23454234F")),
        ValidationField.USER_NAME);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void rejectsMissingName(String name) {
    assertError(() -> validator.validate(request(name, "email@email.com", "677998899", "admin", "23454234F")),
        ValidationField.USER_NAME);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"emailemail.com", "email@emailcom", "email"})
  void rejectsEmailWithoutAtSignOrDot(String email) {
    assertError(() -> validator.validate(request("pablo", email, "677998899", "admin", "23454234F")),
        ValidationField.EMAIL);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void rejectsMissingPhone(String phone) {
    assertError(() -> validator.validate(request("pablo", "email@email.com", phone, "admin", "23454234F")),
        ValidationField.PHONE);
  }

  @Test
  void rejectsUnknownRole() {
    assertError(() -> validator.validate(request("pablo", "email@email.com", "677998899", "user", "23454234F")),
        ValidationField.ROLE);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void rejectsMissingDni(String dni) {
    assertError(() -> validator.validate(request("pablo", "email@email.com", "677998899", "admin", dni)),
        ValidationField.DNI);
  }

  /** 23454234W es el DNI del ejemplo del enunciado: su letra de control es la F. */
  @ParameterizedTest
  @ValueSource(strings = {"23454234W", "1234567Z", "X1234567L"})
  void rejectsDniThatIsNotARealDni(String dni) {
    assertError(() -> validator.validate(request("pablo", "email@email.com", "677998899", "admin", dni)),
        ValidationField.DNI);
  }

  @Test
  void reportsTheFirstInvalidFieldInRequestOrder() {
    assertError(() -> validator.validate(request("pablito", "no-email", null, "user", null)),
        ValidationField.USER_NAME);
  }

  private static CreateUserRequest request(String name, String email, String phone, String rol, String dni) {
    return new CreateUserRequest(name, email, phone, rol, dni);
  }

  private static void assertError(ThrowingCallable action, ValidationField field) {
    assertThatThrownBy(action)
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation " + field.value())
        .extracting("field").isEqualTo(field);
  }
}

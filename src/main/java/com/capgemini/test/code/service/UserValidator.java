package com.capgemini.test.code.service;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import com.capgemini.test.code.model.Role;
import org.springframework.stereotype.Component;

/**
 * Reglas de formato de un usuario nuevo. Comprueba los campos en el orden de la petición y lanza la
 * excepción del primero que no es válido.
 */
@Component
public class UserValidator {

  static final int MAX_NAME_LENGTH = 6;

  /**
   * @return el rol de la petición, ya interpretado
   * @throws ValidationException con el primer campo que no es válido
   */
  public Role validate(CreateUserRequest request) {
    validateName(request.name());
    validateEmail(request.email());
    requireText(request.phone(), ValidationField.PHONE);
    Role role = Role.fromValue(request.rol());
    validateDni(request.dni());
    return role;
  }

  /** Solo el formato y la letra de control: si el DNI está autorizado lo decide la API externa. */
  private static void validateDni(String dni) {
    if (!DniFormat.isValid(dni)) {
      throw new ValidationException(ValidationField.DNI);
    }
  }

  private static void validateName(String name) {
    requireText(name, ValidationField.USER_NAME);
    if (name.length() > MAX_NAME_LENGTH) {
      throw new ValidationException(ValidationField.USER_NAME);
    }
  }

  private static void validateEmail(String email) {
    requireText(email, ValidationField.EMAIL);
    if (!email.contains("@") || !email.contains(".")) {
      throw new ValidationException(ValidationField.EMAIL);
    }
  }

  private static void requireText(String value, ValidationField field) {
    if (value == null || value.isBlank()) {
      throw new ValidationException(field);
    }
  }
}

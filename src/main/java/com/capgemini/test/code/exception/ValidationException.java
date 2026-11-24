package com.capgemini.test.code.exception;

/**
 * Los datos del usuario no cumplen una regla: formato de un campo, DNI rechazado, dato duplicado...
 * Se responde con 409.
 */
public class ValidationException extends RuntimeException {

  private final ValidationField field;

  public ValidationException(ValidationField field) {
    super("error validation " + field.value());
    this.field = field;
  }

  public ValidationField field() {
    return field;
  }
}

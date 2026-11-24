package com.capgemini.test.code.exception;

/**
 * Ya existe otro usuario con el mismo valor en un campo que debe ser único.
 */
public class UserAlreadyExistsException extends ValidationException {

  public UserAlreadyExistsException(ValidationField field) {
    super(field);
  }
}

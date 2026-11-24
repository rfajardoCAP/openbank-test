package com.capgemini.test.code.exception;

/**
 * Campo que no supera una validación. Su valor es el que se devuelve al cliente en el
 * mensaje {@code "error validation <campo>"}.
 */
public enum ValidationField {
  USER_NAME("userName"),
  EMAIL("email"),
  PHONE("phone"),
  ROLE("rol"),
  DNI("dni");

  private final String value;

  ValidationField(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}

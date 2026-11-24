package com.capgemini.test.code.exception;

/**
 * El usuario no existe en la sala consultada. Se responde con 404.
 */
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long id) {
    super("user not found: " + id);
  }
}

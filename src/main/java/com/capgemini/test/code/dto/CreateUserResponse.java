package com.capgemini.test.code.dto;

/**
 * Respuesta de {@code POST /users}: {@code {"id": "<id>"}}, con el id como texto tal y como
 * aparece en el enunciado.
 */
public record CreateUserResponse(String id) {

  public static CreateUserResponse of(Long id) {
    return new CreateUserResponse(String.valueOf(id));
  }
}

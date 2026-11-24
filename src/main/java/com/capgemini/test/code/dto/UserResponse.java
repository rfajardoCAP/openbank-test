package com.capgemini.test.code.dto;

import com.capgemini.test.code.model.User;

/**
 * Respuesta de {@code GET /users/{id}}: los mismos campos que se envían al crear el usuario, más su id.
 */
public record UserResponse(String id, String name, String email, String phone, String rol, String dni) {

  public static UserResponse from(User user) {
    return new UserResponse(String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getPhone(),
        user.getRole().value(), user.getDni());
  }
}

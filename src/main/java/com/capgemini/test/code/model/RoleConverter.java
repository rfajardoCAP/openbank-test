package com.capgemini.test.code.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

/**
 * Guarda el rol en la columna {@code users.role} en minúsculas ({@code admin}, {@code superadmin}),
 * el mismo valor que llega y sale por la API.
 *
 * <p>Al leer solo acepta esos valores exactos: un rol que no está en minúsculas o que no existe es
 * un dato corrupto, no un error de validación del cliente.
 */
@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

  @Override
  public String convertToDatabaseColumn(Role role) {
    return role == null ? null : role.value();
  }

  @Override
  public Role convertToEntityAttribute(String column) {
    if (column == null) {
      return null;
    }
    return Arrays.stream(Role.values())
        .filter(role -> role.value().equals(column))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown role in database: '" + column + "'"));
  }
}

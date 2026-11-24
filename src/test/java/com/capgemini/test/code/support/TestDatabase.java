package com.capgemini.test.code.support;

import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Base de datos de los tests: H2 en memoria, en modo de compatibilidad con PostgreSQL. Es Java puro,
 * así que funciona en cualquier equipo sin Docker, sin binarios nativos y sin permisos de
 * administrador. La comparten todas las clases de test ({@code DB_CLOSE_DELAY=-1} la mantiene viva
 * mientras dure la JVM) y Flyway crea el esquema y los datos iniciales al levantar el contexto.
 *
 * <p>No es PostgreSQL real: lo que depende del motor, como los nombres de las restricciones UNIQUE,
 * no se puede probar aquí.
 */
public final class TestDatabase {

  private static final String URL =
      "jdbc:h2:mem:users;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

  private TestDatabase() {
  }

  public static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> URL);
    registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");
    registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
  }
}

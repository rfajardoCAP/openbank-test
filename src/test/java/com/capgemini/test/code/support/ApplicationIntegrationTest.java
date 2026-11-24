package com.capgemini.test.code.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base de los tests que levantan la aplicación completa (servidor HTTP en un puerto aleatorio)
 * contra H2 (en modo PostgreSQL) y MockServer, embebidos en la JVM. Todas las subclases comparten
 * el mismo contexto de Spring, la misma base de datos y el mismo MockServer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApplicationIntegrationTest {

  @DynamicPropertySource
  static void infrastructure(DynamicPropertyRegistry registry) {
    TestDatabase.registerProperties(registry);
    TestMockServer.registerProperties(registry);
  }

  @BeforeEach
  void resetMockServer() {
    TestMockServer.reset();
  }
}

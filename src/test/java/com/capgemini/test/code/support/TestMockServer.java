package com.capgemini.test.code.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mockserver.integration.ClientAndServer;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * MockServer real, arrancado dentro de la JVM en un puerto libre (sin Docker) y cargado con el mismo
 * {@code docker/mockserver-config/init.json} que usa docker-compose, para que los tests prueben
 * contra los stubs que da el enunciado.
 *
 * <p>Se controla por la API REST de MockServer, igual que el contenedor, para verificar qué
 * peticiones recibió y para añadir expectativas puntuales (por ejemplo, simular una caída).
 */
public final class TestMockServer {

  private static final Path INIT_JSON = Path.of("docker/mockserver-config/init.json");
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  private static ClientAndServer server;

  private TestMockServer() {
  }

  public static void registerProperties(DynamicPropertyRegistry registry) {
    start();
    registry.add("external.service.url", TestMockServer::baseUrl);
  }

  /** Olvida las peticiones recibidas y las expectativas añadidas en los tests, y recarga init.json. */
  public static void reset() {
    control("/mockserver/reset", "");
    control("/mockserver/expectation", readInitJson());
  }

  /**
   * Comprueba que MockServer recibió exactamente una vez la petición descrita.
   *
   * @param httpRequestJson objeto {@code httpRequest} de MockServer, por ejemplo
   *     {@code {"method":"POST","path":"/email","body":{"type":"JSON","json":{...}}}}
   */
  public static boolean receivedOnce(String httpRequestJson) {
    String body = "{\"httpRequest\":" + httpRequestJson + ",\"times\":{\"atLeast\":1,\"atMost\":1}}";
    return control("/mockserver/verify", body) == 202;
  }

  /** Añade una expectativa con prioridad sobre las de init.json. */
  public static void addExpectation(String expectationJson) {
    int status = control("/mockserver/expectation", expectationJson);
    if (status != 201) {
      throw new IllegalStateException("MockServer rejected expectation, status " + status);
    }
  }

  private static String baseUrl() {
    return "http://localhost:" + server.getPort();
  }

  private static int control(String path, String body) {
    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(body))
        .build();
    try {
      return HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    } catch (IOException e) {
      throw new IllegalStateException("MockServer control call failed: " + path, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted calling MockServer: " + path, e);
    }
  }

  private static String readInitJson() {
    try {
      return Files.readString(INIT_JSON);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + INIT_JSON, e);
    }
  }

  private static synchronized void start() {
    if (server != null) {
      return;
    }
    server = ClientAndServer.startClientAndServer();
    reset();
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }
}

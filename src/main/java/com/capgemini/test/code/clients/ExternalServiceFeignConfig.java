package com.capgemini.test.code.clients;

import feign.Client;
import feign.Logger;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;

/**
 * Configuración común de los clientes Feign hacia servicios externos: OkHttp y log completo.
 *
 * <p>No lleva {@code @Configuration} a propósito: se referencia desde
 * {@code @FeignClient(configuration = ...)} y así sus beans solo existen en el contexto de cada
 * cliente, sin registrarse en el contexto principal de la aplicación.
 */
public class ExternalServiceFeignConfig {

  @Bean
  public Client feignClient() {
    return new OkHttpClient();
  }

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}

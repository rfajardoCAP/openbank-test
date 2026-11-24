package com.capgemini.test.code.exception;

/**
 * Un servicio externo no responde o responde con un error inesperado. Se responde con 503.
 */
public class ExternalServiceException extends RuntimeException {

  public ExternalServiceException(String service, Throwable cause) {
    super("external service unavailable: " + service, cause);
  }
}

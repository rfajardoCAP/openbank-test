package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.exception.ExternalServiceException;
import feign.FeignException;
import org.springframework.stereotype.Service;

/**
 * Valida el DNI contra {@code PATCH /check-dni}: un 2xx es DNI válido y un 409 es DNI rechazado.
 * Cualquier otro fallo se trata como servicio no disponible.
 */
@Service
public class DniValidationService {

  private static final String SERVICE_NAME = "check-dni";

  private final DniClient dniClient;

  public DniValidationService(DniClient dniClient) {
    this.dniClient = dniClient;
  }

  public boolean isValid(String dni) {
    try {
      dniClient.check(new CheckDniRequest(dni));
      return true;
    } catch (FeignException.Conflict rejected) {
      return false;
    } catch (FeignException failure) {
      throw new ExternalServiceException(SERVICE_NAME, failure);
    }
  }
}

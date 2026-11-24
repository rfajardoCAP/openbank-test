package com.capgemini.test.code.service;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.model.User;

public interface UserService {

  /**
   * Guarda el usuario en la sala por defecto, le notifica según su rol y devuelve su id.
   *
   * @throws com.capgemini.test.code.exception.ValidationException si algún dato no es válido o ya existe
   * @throws com.capgemini.test.code.exception.ExternalServiceException si la API externa falla; en
   *     ese caso el usuario no queda guardado
   */
  Long create(CreateUserRequest request);

  /**
   * @throws com.capgemini.test.code.exception.UserNotFoundException si el usuario no existe en la sala por defecto
   */
  User get(Long id);
}

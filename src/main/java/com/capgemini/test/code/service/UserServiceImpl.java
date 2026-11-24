package com.capgemini.test.code.service;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.exception.UserAlreadyExistsException;
import com.capgemini.test.code.exception.UserNotFoundException;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import com.capgemini.test.code.repository.UserRepository;
import com.capgemini.test.code.service.notification.NotificationService;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las comprobaciones van de la más barata a la más cara: formato de los campos, email duplicado
 * (base de datos) y DNI (servicio externo). El guardado y la notificación van en la misma
 * transacción: si la notificación falla, el usuario no queda guardado.
 */
@Service
public class UserServiceImpl implements UserService {

  /**
   * Nombres que PostgreSQL da a las restricciones UNIQUE declaradas en {@code V1__init.sql}, y el
   * campo que se devuelve al cliente cuando se incumplen.
   */
  private static final Map<String, ValidationField> UNIQUE_CONSTRAINTS = Map.of(
      "users_email_key", ValidationField.EMAIL,
      "users_dni_key", ValidationField.DNI,
      "users_phone_key", ValidationField.PHONE);

  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final DniValidationService dniValidationService;
  private final NotificationService notificationService;
  private final Long roomId;

  public UserServiceImpl(UserRepository userRepository, UserValidator userValidator,
      DniValidationService dniValidationService, NotificationService notificationService,
      @Value("${app.users.default-room-id}") Long roomId) {
    this.userRepository = userRepository;
    this.userValidator = userValidator;
    this.dniValidationService = dniValidationService;
    this.notificationService = notificationService;
    this.roomId = roomId;
  }

  @Override
  @Transactional
  public Long create(CreateUserRequest request) {
    Role role = userValidator.validate(request);
    ensureEmailIsFree(request.email());
    ensureDniIsValid(request.dni());

    User saved = save(User.builder()
        .name(request.name())
        .email(request.email())
        .phone(request.phone())
        .role(role)
        .dni(request.dni())
        .roomId(roomId)
        .build());
    notificationService.notifyUserSaved(saved);
    return saved.getId();
  }

  @Override
  @Transactional(readOnly = true)
  public User get(Long id) {
    return userRepository.findByIdAndRoomId(id, roomId)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  private void ensureEmailIsFree(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new UserAlreadyExistsException(ValidationField.EMAIL);
    }
  }

  private void ensureDniIsValid(String dni) {
    if (!dniValidationService.isValid(dni)) {
      throw new ValidationException(ValidationField.DNI);
    }
  }

  /**
   * Usa {@code saveAndFlush} para que una restricción UNIQUE incumplida falle aquí y no al confirmar
   * la transacción, y así poder traducirla. La comprobación previa del email no basta: dos
   * peticiones simultáneas pueden pasarla a la vez, y es la base de datos la que decide.
   */
  private User save(User user) {
    try {
      return userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException e) {
      throw translate(e);
    }
  }

  private static RuntimeException translate(DataIntegrityViolationException e) {
    if (e.getCause() instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
      ValidationField field = UNIQUE_CONSTRAINTS.get(violation.getConstraintName().toLowerCase());
      if (field != null) {
        return new UserAlreadyExistsException(field);
      }
    }
    return e;
  }
}

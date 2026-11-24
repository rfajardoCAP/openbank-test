package com.capgemini.test.code.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.exception.ExternalServiceException;
import com.capgemini.test.code.exception.UserAlreadyExistsException;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import com.capgemini.test.code.repository.UserRepository;
import com.capgemini.test.code.service.notification.NotificationService;
import java.sql.SQLException;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final long ROOM_ID = 1L;
  private static final long GENERATED_ID = 42L;
  private static final CreateUserRequest VALID_REQUEST =
      new CreateUserRequest("pablo", "email@email.com", "677998899", "admin", "23454234F");

  @Mock
  private UserRepository userRepository;
  @Mock
  private DniValidationService dniValidationService;
  @Mock
  private NotificationService notificationService;

  private UserServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new UserServiceImpl(userRepository, new UserValidator(), dniValidationService, notificationService,
        ROOM_ID);
  }

  @Nested
  class Create {

    @Test
    void savesTheUserInTheConfiguredRoomAndReturnsItsId() {
      givenValidDniAndFreeEmail();

      Long id = service.create(VALID_REQUEST);

      assertThat(id).isEqualTo(GENERATED_ID);
      ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
      verify(userRepository).saveAndFlush(saved.capture());
      assertThat(saved.getValue())
          .extracting(User::getName, User::getEmail, User::getPhone, User::getRole, User::getDni, User::getRoomId)
          .containsExactly("pablo", "email@email.com", "677998899", Role.ADMIN, "23454234F", ROOM_ID);
    }

    @Test
    void notifiesTheSavedUser() {
      givenValidDniAndFreeEmail();

      service.create(VALID_REQUEST);

      ArgumentCaptor<User> notified = ArgumentCaptor.forClass(User.class);
      verify(notificationService).notifyUserSaved(notified.capture());
      assertThat(notified.getValue().getId()).isEqualTo(GENERATED_ID);
    }

    @Test
    void propagatesNotificationFailureSoTheTransactionIsRolledBack() {
      givenValidDniAndFreeEmail();
      ExternalServiceException failure = new ExternalServiceException("email", new RuntimeException("down"));
      doThrow(failure).when(notificationService).notifyUserSaved(any());

      assertThatThrownBy(() -> service.create(VALID_REQUEST)).isSameAs(failure);
    }

    @Test
    void rejectsDuplicatedEmailBeforeCallingTheDniService() {
      when(userRepository.existsByEmail("email@email.com")).thenReturn(true);

      assertThatThrownBy(() -> service.create(VALID_REQUEST))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("error validation email");

      verifyNoInteractions(dniValidationService, notificationService);
      verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDniRefusedByTheExternalService() {
      when(userRepository.existsByEmail(anyString())).thenReturn(false);
      when(dniValidationService.isValid("23454234F")).thenReturn(false);

      assertThatThrownBy(() -> service.create(VALID_REQUEST))
          .isInstanceOf(ValidationException.class)
          .hasMessage("error validation dni");

      verify(userRepository, never()).saveAndFlush(any());
      verifyNoInteractions(notificationService);
    }

    @Test
    void rejectsInvalidFieldsWithoutTouchingAnyDependency() {
      CreateUserRequest invalid = new CreateUserRequest("pablito", "email@email.com", "677998899", "admin", "23454234F");

      assertThatThrownBy(() -> service.create(invalid))
          .isInstanceOf(ValidationException.class)
          .extracting("field").isEqualTo(ValidationField.USER_NAME);

      verifyNoInteractions(userRepository, dniValidationService, notificationService);
    }

    @Test
    void rethrowsIntegrityViolationsItDoesNotKnow() {
      givenValidDniAndFreeEmailOnly();
      DataIntegrityViolationException unknown = uniqueViolation("fk_room");
      when(userRepository.saveAndFlush(any())).thenThrow(unknown);

      assertThatThrownBy(() -> service.create(VALID_REQUEST)).isSameAs(unknown);
    }

    private void givenValidDniAndFreeEmail() {
      givenValidDniAndFreeEmailOnly();
      when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
        User user = invocation.getArgument(0);
        return User.builder().id(GENERATED_ID).name(user.getName()).email(user.getEmail()).phone(user.getPhone())
            .role(user.getRole()).dni(user.getDni()).roomId(user.getRoomId()).build();
      });
    }

    private void givenValidDniAndFreeEmailOnly() {
      when(userRepository.existsByEmail(anyString())).thenReturn(false);
      when(dniValidationService.isValid(anyString())).thenReturn(true);
    }

    private DataIntegrityViolationException uniqueViolation(String constraint) {
      return new DataIntegrityViolationException("duplicate",
          new ConstraintViolationException("duplicate", new SQLException("duplicate"), constraint));
    }
  }

  @Nested
  class Get {

    @Test
    void returnsTheUserOfTheConfiguredRoom() {
      User stored = User.builder().id(5L).name("pablo").role(Role.ADMIN).roomId(ROOM_ID).build();
      when(userRepository.findByIdAndRoomId(5L, ROOM_ID)).thenReturn(Optional.of(stored));

      assertThat(service.get(5L)).isSameAs(stored);
    }
  }
}

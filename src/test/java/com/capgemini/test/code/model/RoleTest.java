package com.capgemini.test.code.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RoleTest {

  @Test
  void parsesAdminIgnoringCase() {
    assertThat(Role.fromValue("admin")).isEqualTo(Role.ADMIN);
    assertThat(Role.fromValue("ADMIN")).isEqualTo(Role.ADMIN);
  }

  @Test
  void parsesSuperadmin() {
    assertThat(Role.fromValue("superadmin")).isEqualTo(Role.SUPERADMIN);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"user", "root", " admin"})
  void rejectsAnyOtherValue(String value) {
    assertThatThrownBy(() -> Role.fromValue(value))
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation rol")
        .extracting("field").isEqualTo(ValidationField.ROLE);
  }

  @Test
  void adminIsNotifiedByEmailAndSuperadminBySms() {
    assertThat(Role.ADMIN.notificationChannel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(Role.SUPERADMIN.notificationChannel()).isEqualTo(NotificationChannel.SMS);
  }

  @Test
  void exposesLowercaseValue() {
    assertThat(Role.SUPERADMIN.value()).isEqualTo("superadmin");
  }
}

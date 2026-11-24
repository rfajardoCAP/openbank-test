package com.capgemini.test.code.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RoleConverterTest {

  private final RoleConverter converter = new RoleConverter();

  @ParameterizedTest
  @CsvSource({"ADMIN, admin", "SUPERADMIN, superadmin"})
  void storesTheRoleInLowercase(Role role, String column) {
    assertThat(converter.convertToDatabaseColumn(role)).isEqualTo(column);
  }

  @ParameterizedTest
  @CsvSource({"admin, ADMIN", "superadmin, SUPERADMIN"})
  void readsTheLowercaseRole(String column, Role role) {
    assertThat(converter.convertToEntityAttribute(column)).isEqualTo(role);
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMIN", "user", ""})
  void failsOnAValueThatIsNotAStoredRole(String column) {
    assertThatThrownBy(() -> converter.convertToEntityAttribute(column))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(column);
  }

  @Test
  void keepsNullAsNull() {
    assertThat(converter.convertToDatabaseColumn(null)).isNull();
    assertThat(converter.convertToEntityAttribute(null)).isNull();
  }
}

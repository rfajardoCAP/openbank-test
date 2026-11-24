package com.capgemini.test.code.model;

import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.exception.ValidationField;
import java.util.Arrays;

/**
 * Roles permitidos y canal por el que se notifica a cada uno.
 */
public enum Role {
  ADMIN("admin", NotificationChannel.EMAIL),
  SUPERADMIN("superadmin", NotificationChannel.SMS);

  private final String value;
  private final NotificationChannel notificationChannel;

  Role(String value, NotificationChannel notificationChannel) {
    this.value = value;
    this.notificationChannel = notificationChannel;
  }

  public static Role fromValue(String value) {
    return Arrays.stream(values())
        .filter(role -> role.value.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new ValidationException(ValidationField.ROLE));
  }

  public String value() {
    return value;
  }

  public NotificationChannel notificationChannel() {
    return notificationChannel;
  }
}

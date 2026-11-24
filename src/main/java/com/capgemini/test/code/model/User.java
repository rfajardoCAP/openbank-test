package com.capgemini.test.code.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Usuario de una sala, mapeado a la tabla {@code users}. El esquema lo define Flyway
 * ({@code ddl-auto: none}). Se construye con el builder para no confundir campos del mismo tipo.
 *
 * <p>La entidad no valida: las reglas de un usuario nuevo están en {@code UserValidator}, y así
 * los datos iniciales de la base de datos, que no las cumplen, pueden leerse igualmente.
 */
@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String dni;

  @Column(nullable = false)
  private String phone;

  @Convert(converter = RoleConverter.class)
  @Column(nullable = false)
  private Role role;

  @Column(name = "room_id")
  private Long roomId;

  public NotificationChannel notificationChannel() {
    return role.notificationChannel();
  }
}

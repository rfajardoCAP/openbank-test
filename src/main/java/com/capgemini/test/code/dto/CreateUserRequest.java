package com.capgemini.test.code.dto;

/**
 * Cuerpo de {@code POST /users}. Los nombres de los campos son los del enunciado ({@code rol}).
 */
public record CreateUserRequest(String name, String email, String phone, String rol, String dni) {
}

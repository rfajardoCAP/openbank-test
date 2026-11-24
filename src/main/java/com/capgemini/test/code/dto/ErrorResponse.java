package com.capgemini.test.code.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Cuerpo de todas las respuestas de error: {@code {"code": <estado>, "message": "<detalle>"}}.
 */
public record ErrorResponse(int code, String message) {

  public static ResponseEntity<ErrorResponse> of(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
  }
}

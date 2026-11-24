package com.capgemini.test.code.controller;

import com.capgemini.test.code.dto.ErrorResponse;
import com.capgemini.test.code.exception.ExternalServiceException;
import com.capgemini.test.code.exception.UserNotFoundException;
import com.capgemini.test.code.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce las excepciones a respuestas {@code {"code", "message"}}.
 */
@RestControllerAdvice
public class RestExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  /** Formato pedido: {@code 409 {"code": 409, "message": "error validation <campo>"}}. */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
    return ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException e) {
    return ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException e) {
    log.warn("External service failure, request rolled back: {}", e.getMessage(), e.getCause());
    return ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
    return ErrorResponse.of(HttpStatus.BAD_REQUEST, "malformed request body");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return ErrorResponse.of(HttpStatus.BAD_REQUEST, "invalid value for " + e.getName());
  }
}

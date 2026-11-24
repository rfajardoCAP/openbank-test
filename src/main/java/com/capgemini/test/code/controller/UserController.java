package com.capgemini.test.code.controller;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.CreateUserResponse;
import com.capgemini.test.code.dto.UserResponse;
import com.capgemini.test.code.service.UserService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa HTTP: traduce entre JSON y {@link UserService}. No contiene reglas de negocio.
 */
@RestController
@RequestMapping(UserController.BASE_PATH)
public class UserController {

  static final String BASE_PATH = "/users";

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<CreateUserResponse> create(@RequestBody CreateUserRequest request) {
    Long id = userService.create(request);
    return ResponseEntity.created(URI.create(BASE_PATH + "/" + id)).body(CreateUserResponse.of(id));
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable("id") Long id) {
    return UserResponse.from(userService.get(id));
  }
}

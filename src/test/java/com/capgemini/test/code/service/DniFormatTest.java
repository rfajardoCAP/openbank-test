package com.capgemini.test.code.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DniFormatTest {

  @ParameterizedTest
  @ValueSource(strings = {"12345678Z", "87654321X", "23454234F", "00000000T", "99999999R"})
  void acceptsDniWithItsControlLetter(String dni) {
    assertThat(DniFormat.isValid(dni)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345678z", "99999999r"})
  void acceptsTheControlLetterInLowercase(String dni) {
    assertThat(DniFormat.isValid(dni)).isTrue();
  }

  /** 23454234W es el ejemplo del enunciado y 99999999w el que rechaza la API externa: su letra es F y R. */
  @ParameterizedTest
  @ValueSource(strings = {"23454234W", "99999999w", "12345678A"})
  void rejectsDniWithAWrongControlLetter(String dni) {
    assertThat(DniFormat.isValid(dni)).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"1234567Z", "123456789Z", "12345678", "ABCDEFGHZ", "X1234567L", " 12345678Z",
      "12345678Z ", "12345678-Z", "1234 5678Z"})
  void rejectsWhatIsNotEightDigitsAndALetter(String dni) {
    assertThat(DniFormat.isValid(dni)).isFalse();
  }
}

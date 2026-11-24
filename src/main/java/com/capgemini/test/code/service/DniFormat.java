package com.capgemini.test.code.service;

import java.util.regex.Pattern;

/**
 * Formato del DNI español: 8 dígitos y la letra de control que les corresponde. La letra es la
 * posición {@code número % 23} de {@link #CONTROL_LETTERS}, y se acepta también en minúscula.
 *
 * <p>No admite NIE (X/Y/Z inicial), espacios ni guiones.
 */
final class DniFormat {

  private static final Pattern DIGITS_AND_LETTER = Pattern.compile("\\d{8}[A-Za-z]");
  private static final String CONTROL_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

  private DniFormat() {
  }

  static boolean isValid(String dni) {
    if (dni == null || !DIGITS_AND_LETTER.matcher(dni).matches()) {
      return false;
    }
    int number = Integer.parseInt(dni.substring(0, 8));
    char letter = Character.toUpperCase(dni.charAt(8));
    return CONTROL_LETTERS.charAt(number % CONTROL_LETTERS.length()) == letter;
  }
}

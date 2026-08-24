package si.result.eearly.exception;

import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException {
  final int statusCode;

  public KeycloakException(int statusCode) {
    this.statusCode = statusCode;
  }
}

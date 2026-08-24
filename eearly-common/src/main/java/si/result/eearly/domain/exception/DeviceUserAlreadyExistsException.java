package si.result.eearly.domain.exception;

public class DeviceUserAlreadyExistsException extends RuntimeException {
  public DeviceUserAlreadyExistsException(String message) {
    super(message);
  }
}

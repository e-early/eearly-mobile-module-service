package si.result.eearly.domain.exception;

public class LockCouldNotBeAcquiredException extends RuntimeException  {

  public LockCouldNotBeAcquiredException(String message) {
    super(message);
  }
}

package si.result.eearly.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlreadyExistsException extends RuntimeException {

  private si.result.eearly.genproto.ErrorCode errorCode;


  public AlreadyExistsException(String message, si.result.eearly.genproto.ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public AlreadyExistsException(String message, Throwable cause, si.result.eearly.genproto.ErrorCode errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
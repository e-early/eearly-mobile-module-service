package si.result.eearly.exception;

import lombok.Getter;
import lombok.Setter;

import si.result.eearly.genproto.ErrorCode;

@Getter
@Setter
public class ServiceInvalidRequestException extends RuntimeException {

    private ErrorCode errorCode;


    public ServiceInvalidRequestException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceInvalidRequestException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
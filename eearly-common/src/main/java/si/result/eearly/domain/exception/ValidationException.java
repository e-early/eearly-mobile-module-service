package si.result.eearly.domain.exception;

public class ValidationException extends Exception {

    private final ErrorCode errorCode;

    public ValidationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

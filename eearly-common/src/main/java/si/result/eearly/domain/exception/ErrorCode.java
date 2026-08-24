package si.result.eearly.domain.exception;

public enum ErrorCode {

    INTERNAL_ERROR(1),
    VALIDATION_ERROR(2),
    ALREADY_EXISTS(3);

    private int numVal;

    ErrorCode(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}

package cz.mp.construction_site_diary.exception;

public class AuthenticationException extends RuntimeException {

    private final ErrorCode errorCode;

    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthenticationException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        INVALID_CREDENTIALS,
        USER_DISABLED,
        SESSION_EXPIRED,
        KEYCLOAK_ERROR
    }
}
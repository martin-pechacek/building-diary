package cz.mp.building_diary.exception;

public class UserRegistrationException extends RuntimeException {

    private final ErrorCode errorCode;

    public UserRegistrationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserRegistrationException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        USERNAME_EXISTS,
        EMAIL_EXISTS,
        ROLE_NOT_FOUND,
        KEYCLOAK_ERROR
    }
}
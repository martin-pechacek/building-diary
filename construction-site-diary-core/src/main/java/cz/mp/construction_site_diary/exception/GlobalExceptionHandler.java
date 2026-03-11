package cz.mp.construction_site_diary.exception;

import cz.mp.construction_site_diary.dto.ErrorDto;
import cz.mp.construction_site_diary.exception.VerificationTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorDto("Validation failed", HttpStatus.BAD_REQUEST.value(), errors));
    }

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<ErrorDto> handleRegistrationException(UserRegistrationException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case USERNAME_EXISTS, EMAIL_EXISTS -> HttpStatus.CONFLICT;
            case ROLE_NOT_FOUND -> HttpStatus.INTERNAL_SERVER_ERROR;
            case KEYCLOAK_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
        };

        LOG.error("Registration failed: {}", ex.getMessage(), ex);

        return ResponseEntity.status(status)
                .body(new ErrorDto(ex.getMessage(), status.value()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDto> handleAuthenticationException(AuthenticationException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case INVALID_CREDENTIALS, USER_DISABLED, SESSION_EXPIRED -> HttpStatus.UNAUTHORIZED;
            case KEYCLOAK_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
        };

        LOG.error("Authentication failed: {}", ex.getMessage(), ex);

        return ResponseEntity.status(status)
                .body(new ErrorDto(ex.getMessage(), status.value()));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProjectNotFoundException(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(ProjectStateException.class)
    public ResponseEntity<ErrorDto> handleProjectStateException(ProjectStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(DiaryEntryAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleDiaryEntryAlreadyExistsException(DiaryEntryAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(DiaryEntryNotFoundException.class)
    public ResponseEntity<ErrorDto> handleDiaryEntryNotFoundException(DiaryEntryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ErrorDto> handleExportException(ExportException ex) {
        LOG.error("Export failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(CoordinatesNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCoordinatesNotFoundException(CoordinatesNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(VerificationTokenException.class)
    public ResponseEntity<ErrorDto> handleVerificationTokenException(VerificationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorDto> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorDto(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        LOG.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
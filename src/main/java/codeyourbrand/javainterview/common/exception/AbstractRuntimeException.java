package codeyourbrand.javainterview.common.exception;

import codeyourbrand.javainterview.common.model.ApplicationErrorCode;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class AbstractRuntimeException extends RuntimeException {
    private final String uuid;
    private final ApplicationErrorCode applicationErrorCode;

    @Getter(AccessLevel.NONE)
    private static final ApplicationErrorCode DEFAULT_APPLICATION_ERROR_CODE =
            ApplicationErrorCode.GENERIC_SERVER_ERROR;

    protected AbstractRuntimeException(Throwable cause, String message) {
        this(cause, message, DEFAULT_APPLICATION_ERROR_CODE);
    }

    protected AbstractRuntimeException(String message) {
        this(null, message, DEFAULT_APPLICATION_ERROR_CODE);
    }

    protected AbstractRuntimeException(Throwable cause) {
        this(cause, null, DEFAULT_APPLICATION_ERROR_CODE);
    }

    protected AbstractRuntimeException(ApplicationErrorCode applicationErrorCode) {
        this(null, null, applicationErrorCode);
    }

    protected AbstractRuntimeException(String message, ApplicationErrorCode applicationErrorCode) {
        this(null, message, applicationErrorCode);
    }

    protected AbstractRuntimeException(Throwable cause, ApplicationErrorCode applicationErrorCode) {
        this(cause, null, applicationErrorCode);
    }

    protected AbstractRuntimeException(Throwable cause, String message, ApplicationErrorCode applicationErrorCode) {
        super(message, cause);
        this.applicationErrorCode = applicationErrorCode;
        if (cause instanceof AbstractRuntimeException uuidCause) {
            this.uuid = uuidCause.getUuid();
        } else {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[%s][%s]: message=%s, errorCode=%s, cause=%s",
                getUuid(), getClass().getSimpleName(), getMessage(), getApplicationErrorCode(), getCause());
    }
}

package org.orquanet.webauthn.webauthn.attestation.validation.exception;

public class X5cValidationException extends RuntimeException {
    public X5cValidationException() {
    }

    public X5cValidationException(String message) {
        super(message);
    }

    public X5cValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public X5cValidationException(Throwable cause) {
        super(cause);
    }

    public X5cValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

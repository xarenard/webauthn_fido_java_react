package org.orquanet.webauthn.webauthn.attestation.validation.exception;

public class X5cAttestationValidationException extends RuntimeException {
    public X5cAttestationValidationException(String message) {
        super(message);
    }

    public X5cAttestationValidationException(Throwable cause) {
        super(cause);
    }
}

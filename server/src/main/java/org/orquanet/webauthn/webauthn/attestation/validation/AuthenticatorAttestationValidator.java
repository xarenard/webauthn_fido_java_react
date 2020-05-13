package org.orquanet.webauthn.webauthn.attestation.validation;

import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.clientdata.ClientDataRegistrationValidation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.AttestationValidator;

public class AuthenticatorAttestationValidator {

    private ClientDataRegistrationValidation clientDataRegistrationValidation;
    private AttestationValidator attestationValidator;

    public void validate(AuthenticatorAttestation authenticatorAttestation, String challenge) {
        this.clientDataRegistrationValidation.validate(authenticatorAttestation.getClientDataJSON(),challenge);
        this.attestationValidator.verify(authenticatorAttestation);
    }

    public AuthenticatorAttestationValidator(ClientDataRegistrationValidation clientDataRegistrationValidation, AttestationValidator attestationValidator){
        this.clientDataRegistrationValidation = clientDataRegistrationValidation;
        this.attestationValidator = attestationValidator;
    }



}

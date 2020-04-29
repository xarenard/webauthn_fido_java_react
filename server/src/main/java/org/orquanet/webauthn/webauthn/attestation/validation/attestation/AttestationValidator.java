/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orquanet.webauthn.webauthn.attestation.validation.attestation;


import org.orquanet.webauthn.webauthn.attestation.constant.AttestationStatementFormat;
import org.orquanet.webauthn.webauthn.attestation.exception.AttestationFormatException;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.fido2f.Fido2fAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.PackedAttestationValidatorResolver;

public class AttestationValidator {

    private Fido2fAttestationValidator fido2FAttestationValidator;
    private PackedAttestationValidatorResolver packedAttestationValidatorResolver;

    public AttestationValidator(final Fido2fAttestationValidator fido2FAttestationValidator,
                                final PackedAttestationValidatorResolver packedAttestationValidatorResolver) {
        this.fido2FAttestationValidator = fido2FAttestationValidator;
        this.packedAttestationValidatorResolver = packedAttestationValidatorResolver;
    }

    public void verify(AuthenticatorAttestation authenticatorAttestation) {
        Boolean isValid = Boolean.FALSE;

        AttestationStatementFormat attFmt = authenticatorAttestation.getAttestation().getFmtEnum();
        switch (attFmt) {
            case PACKED:
                isValid = packedAttestationValidatorResolver.resolve(authenticatorAttestation).verify(authenticatorAttestation);
                break;
            case FIDOU2F:
                isValid = fido2FAttestationValidator.verify(authenticatorAttestation);
                break;
            default:
                throw new AttestationFormatException("Attestation Format Not Supported");
        }

        if (!isValid) {
            throw new AttestationValidationException("Invalid Attestation Signature");
        }
    }


}

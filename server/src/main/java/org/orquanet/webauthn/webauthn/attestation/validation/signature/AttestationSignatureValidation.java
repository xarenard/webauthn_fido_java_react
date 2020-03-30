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

package org.orquanet.webauthn.webauthn.attestation.validation.signature;


import org.orquanet.webauthn.webauthn.attestation.constant.AttestationStatementFormat;
import org.orquanet.webauthn.webauthn.attestation.exception.AttestationFormatException;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.orquanet.webauthn.webauthn.attestation.validation.signature.fido2f.Fido2fAttestationSignatureValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.signature.packed.PackedAttestationSignatureValidator;

import java.util.function.Function;

public class AttestationSignatureValidation implements Function<AuthenticatorAttestation, AuthenticatorAttestation> {

    private Fido2fAttestationSignatureValidator fido2fAttestationSignatureValidator;
    private PackedAttestationSignatureValidator packedAttestationSignatureValidator;

    public AttestationSignatureValidation(final Fido2fAttestationSignatureValidator fido2fAttestationSignatureValidator,
                                          final PackedAttestationSignatureValidator packedAttestationSignatureValidator) {
        this.fido2fAttestationSignatureValidator = fido2fAttestationSignatureValidator;
        this.packedAttestationSignatureValidator = packedAttestationSignatureValidator;
    }

    public AuthenticatorAttestation verify(AuthenticatorAttestation authenticatorAttestation) {
        Boolean validSignature = Boolean.FALSE;

        AttestationStatementFormat attFmt = authenticatorAttestation.getAttestation().getFmtEnum();
        switch (attFmt) {
            case PACKED:
                validSignature = packedAttestationSignatureValidator.verify(authenticatorAttestation);
                break;
            case FIDOU2F:
                validSignature = fido2fAttestationSignatureValidator.verify(authenticatorAttestation);
                break;
            default:
                throw new AttestationFormatException("Attestation Format Not Supported");
        }

        if (!validSignature) {
            throw new AttestationValidationException("Invalid Attestation Signature");
        }

        return authenticatorAttestation;
    }

    @Override
    public AuthenticatorAttestation apply(AuthenticatorAttestation authenticatorAttestation) {
        return this.verify(authenticatorAttestation);
    }
}

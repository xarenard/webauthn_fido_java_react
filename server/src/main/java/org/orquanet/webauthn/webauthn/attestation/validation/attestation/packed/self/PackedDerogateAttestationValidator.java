/*
 * Copyright 2018 the original author or authors.
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

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.self;

import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.PackedAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.X5cAttestationValidationException;

import java.security.cert.X509Certificate;

public class PackedDerogateAttestationValidator implements PackedAttestationValidator {

    private CoseMapper coseMapper = new CoseMapper();

    @Override
    public void verify(AuthenticatorAttestation authenticatorAttestation) {
        KeyInfo keyInfo = coseMapper
                .keyInfo(authenticatorAttestation.getAttestation().getAuthenticatorData().getCredentialPublicKey());

        this.verifySignature(authenticatorAttestation, keyInfo);

    }

    @Override
    public void verifyX509Certificate(X509Certificate x509Certificate) {
        throw new X5cAttestationValidationException("X509 not supported in self attestation");
    }


}

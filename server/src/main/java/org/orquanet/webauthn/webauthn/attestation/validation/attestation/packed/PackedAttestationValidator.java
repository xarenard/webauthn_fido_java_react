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

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;

import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import java.util.Base64;

public interface PackedAttestationValidator {

    void verifyX509Certificate(X509Certificate x509Certificate);

    default void verifySignature(AuthenticatorAttestation authenticatorAttestation, KeyInfo keyInfo) {

        try {
            byte[] clientDataRaw = Base64.getUrlDecoder().decode(authenticatorAttestation.getClientDataJSON());
            byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);
            ByteArrayOutputStream verificationDataStream = new ByteArrayOutputStream();
            verificationDataStream.write(authenticatorAttestation.getAttestation().getAuthenticatorData().getAuthData());
            verificationDataStream.write(clientDataHash);
            byte[] verificationData = verificationDataStream.toByteArray();
            verificationDataStream.close();

            SignatureVerifier signatureVerifierObject = new SignatureVerifier();
            boolean validSignature = signatureVerifierObject.validate(verificationData, Base64.getDecoder().decode(authenticatorAttestation.getAttestation().getSignature().getBytes()), keyInfo);
            if(!validSignature){
                throw new AttestationValidationException("Invalid Exception");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new AttestationValidationException(e.getMessage());
        }
    }

    public void verify(AuthenticatorAttestation authenticatorAttestation);
}

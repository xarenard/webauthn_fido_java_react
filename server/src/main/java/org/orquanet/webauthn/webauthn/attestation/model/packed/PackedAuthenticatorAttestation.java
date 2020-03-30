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

package org.orquanet.webauthn.webauthn.attestation.model.packed;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@SuperBuilder
@Getter
public class PackedAuthenticatorAttestation extends AuthenticatorAttestation {

    @Autowired
    @Qualifier("base64urldecoder")
    Base64.Decoder base64Decoder;

    public boolean verifySignature()  throws Exception{
        CoseMapper cose = new CoseMapper();
        KeyInfo keyinfo = cose.keyInfo(getAttestation().getAuthenticatorData().getCredentialPublicKey());

        byte[] clientDataHash = DigestUtils.sha256(base64Decoder.decode(getClientDataJSON().getBytes()));
        ByteArrayOutputStream verificationDataStream = new ByteArrayOutputStream();
        verificationDataStream.write(getAttestation().getAuthenticatorData().getAuthData());
        verificationDataStream.write(clientDataHash);

        byte[] verificationData = verificationDataStream.toByteArray();
        verificationDataStream.close();

        SignatureVerifier signatureVerifierObject = new SignatureVerifier();
        return signatureVerifierObject.validate(verificationData, Base64.getDecoder().decode(getAttestation().getSignature().getBytes()), keyinfo);
    }
}

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

package org.orquanet.webauthn.webauthn.attestation.model.fido;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.crypto.cose.ec.constant.ECCurve;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Optional;

@SuperBuilder
@Getter


public class Fido2fAuthenticatorAttestation extends AuthenticatorAttestation {

    @Autowired
    @Qualifier("base64urldecoder")
    Base64.Decoder base64Decoder;
    public boolean verifySignature() throws Exception{
        CoseMapper cose = new CoseMapper();
        KeyInfo keyinfo = cose.keyInfo(getAttestation().getAuthenticatorData().getCredentialPublicKey());

        BigInteger x = ((ECPublicKey)(keyinfo.getPublicKey())).getW().getAffineX();
        BigInteger y = ((ECPublicKey)(keyinfo.getPublicKey())).getW().getAffineY();

        ByteArrayOutputStream ansiPublicKeyOutputStream = new ByteArrayOutputStream();
        ansiPublicKeyOutputStream.write(0x04);
        ansiPublicKeyOutputStream.write(x.toByteArray());
        ansiPublicKeyOutputStream.write(y.toByteArray());
        byte[] ansiPublicKey = ansiPublicKeyOutputStream.toByteArray();

        byte[] clientDataRaw = base64Decoder.decode(getClientDataJSON());
        byte [] clientDataHash = DigestUtils.sha256(clientDataRaw);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x00);
        out.write(getAttestation().getAuthenticatorData().getRpidHash());
        out.write(clientDataHash);
        out.write(getAttestation().getAuthenticatorData().getCredentialId());
        out.write(ansiPublicKey);

        byte[] verificationData = out.toByteArray();

        PublicKey verificationPublicKey = null;
        //StringReader reader = new StringReader(attestatio.getAttestationStatement().getX5c()[0]);
        if( getAttestation().getX5c().isPresent()) {
            Optional<byte[]> op = getAttestation().getX5c().get().stream().findFirst();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(op.get()));
            verificationPublicKey = cert.getPublicKey();
        }
        KeyInfo keyInfo = KeyInfo.builder().publicKey(verificationPublicKey).algoritm(ECCurve.P256).build();
        SignatureVerifier signatureVerifierObject = new SignatureVerifier();
        return signatureVerifierObject.validate(verificationData,Base64.getDecoder().decode(getAttestation().getSignature()),keyInfo);
    }
}

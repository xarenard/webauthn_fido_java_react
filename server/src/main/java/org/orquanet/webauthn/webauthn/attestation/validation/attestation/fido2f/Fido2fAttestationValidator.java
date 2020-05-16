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

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.fido2f;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.KeyType;
import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.crypto.utils.X509Utils;
import org.orquanet.webauthn.webauthn.attestation.model.Attestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Optional;

public class Fido2fAttestationValidator  {


    @Autowired
    @Qualifier("base64urldecoder")
    private Base64.Decoder base64Decoder;

    private static Logger LOGGER = LoggerFactory.getLogger(Fido2fAttestationValidator.class);

    public void verify(AuthenticatorAttestation authenticatorAttestation) {
        if (authenticatorAttestation == null) {
            throw new IllegalArgumentException();
        }

        try {
            Attestation attestation = authenticatorAttestation.getAttestation();
            String clientDataJSON = authenticatorAttestation.getClientDataJSON();
            CoseMapper cose = new CoseMapper();
            KeyInfo credentialKeyInfo = cose.keyInfo(attestation.getAuthenticatorData().getCredentialPublicKey());
            if(! KeyType.ECDSA.equals(credentialKeyInfo.getKeyType())){
                throw new AttestationValidationException("Non Compatible key type. Only ECDSA is supported");
            }
            BigInteger x = ((ECPublicKey) (credentialKeyInfo.getPublicKey())).getW().getAffineX();
            BigInteger y = ((ECPublicKey) (credentialKeyInfo.getPublicKey())).getW().getAffineY();

            ByteArrayOutputStream ansiPublicKeyOutputStream = new ByteArrayOutputStream();
            ansiPublicKeyOutputStream.write(0x04);
            ansiPublicKeyOutputStream.write(x.toByteArray());
            ansiPublicKeyOutputStream.write(y.toByteArray());
            byte[] ansiPublicKey = ansiPublicKeyOutputStream.toByteArray();

            byte[] clientDataRaw = base64Decoder.decode(clientDataJSON);
            byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(0x00);
            out.write(attestation.getAuthenticatorData().getRpidHash());
            out.write(clientDataHash);
            out.write(attestation.getAuthenticatorData().getCredentialId());
            out.write(ansiPublicKey);

            byte[] verificationData = out.toByteArray();

            PublicKey verificationPublicKey = null;
            //StringReader reader = new StringReader(attestatio.getAttestationStatement().getX5c()[0]);
            if (attestation.getX5c().isPresent()) {
                Optional<byte[]> op = attestation.getX5c().get().stream().findFirst();
                X509Certificate x509Certificate = X509Utils.x509CertificateFromBytesArray(op.get());
                verificationPublicKey = x509Certificate.getPublicKey();
            }
            KeyInfo keyInfo = KeyInfo.builder()
                    .publicKey(verificationPublicKey)
                    .coseAlgorithm(CoseAlgorithm.ES256)
                    .build();
            SignatureVerifier signatureVerifierObject = new SignatureVerifier();
            boolean isValidSignature = signatureVerifierObject.validate(verificationData, Base64.getDecoder().decode(attestation.getSignature()), keyInfo);
            if(!isValidSignature){
                throw new AttestationValidationException("Invalid Signature");
            }
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error(e.getMessage());
            throw new AttestationValidationException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new AttestationValidationException(e);
        }
    }


}



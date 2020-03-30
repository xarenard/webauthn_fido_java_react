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

package org.orquanet.webauthn.webauthn.assertion.validation.signature;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.assertion.data.AuthenticatorAssertion;
import org.orquanet.webauthn.webauthn.assertion.validation.signature.exception.AssertionSignatureValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AssertionSignatureVerifier {

    @Autowired
    @Qualifier("base64urldecoder")
    Base64.Decoder base64Decoder;

    public static Logger LOGGER = LoggerFactory.getLogger(AssertionSignatureVerifier.class);

    public boolean verify(AuthenticatorAssertion authenticatorAssertion, KeyInfo keyInfo){

        boolean signatureValid;
        try {
            String clientDataJSON = authenticatorAssertion.getResponse().getClientDataJSON();
            byte[] clientDataRaw = Base64.getDecoder().decode(clientDataJSON);
            byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);

            String authenticationData = authenticatorAssertion.getResponse().getAuthenticatorData();
            byte[] authData = Base64.getDecoder().decode(authenticationData.getBytes());

            ByteArrayOutputStream o = new ByteArrayOutputStream();
            o.write(authData);
            o.write(clientDataHash);
            o.close();

            byte[] verificationData = o.toByteArray();

            SignatureVerifier signatureVerifierO = new SignatureVerifier();
            signatureValid = signatureVerifierO.validate(verificationData,
                    Base64.getDecoder().decode(authenticatorAssertion.getResponse().getSignature()), keyInfo);
        }
        catch(IOException | NoSuchAlgorithmException e){
            LOGGER.error(e.getMessage());
            throw new AssertionSignatureValidationException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new AssertionSignatureValidationException(e);
        }

        return signatureValid;
    }
}

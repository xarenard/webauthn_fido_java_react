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


package org.orquanet.webauthn.webauthn.common.authdata;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.webauthn.assertion.data.AuthenticatorAssertion;
import org.orquanet.webauthn.webauthn.attestation.data.AttestationObject;
import org.orquanet.webauthn.webauthn.attestation.exception.AttestationException;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorData;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;
import org.orquanet.webauthn.webauthn.common.constant.AuthenticatorOperationType;
import org.orquanet.webauthn.webauthn.exception.AuthenticatorDataException;

import java.util.Arrays;
import java.util.Base64;

public class AuthenticatorDataReader {

    public static final int USER_PRESENT = 1;
    public static final int USER_VERIFIED = 1;

    private WebAuthnConfig webAuthnConfig;

    public AuthenticatorDataReader(final WebAuthnConfig webAuthnConfig){
        this.webAuthnConfig = webAuthnConfig;
    }

    public AuthenticatorData read(AttestationObject attestationObject){

        AuthenticatorData.AuthenticatorDataBuilder attestationDataBuilder = AuthenticatorData.builder();

        byte[] authdata = Base64.getDecoder().decode(attestationObject.getAuthData());
        return this.read(authdata,AuthenticatorOperationType.MAKE_CREDENTIAL);

    }

    public AuthenticatorData read(AuthenticatorAssertion authenticatorAssertion){

        AuthenticatorData.AuthenticatorDataBuilder attestationDataBuilder = AuthenticatorData.builder();

        byte[] authdata = Base64.getDecoder().decode(authenticatorAssertion.getResponse().getAuthenticatorData());
        return this.read(authdata,AuthenticatorOperationType.GET_ASSERTION);

    }

    protected AuthenticatorData read(byte[] authData, AuthenticatorOperationType authenticatorOperationType){

        AuthenticatorData.AuthenticatorDataBuilder attestationDataBuilder = AuthenticatorData.builder();


        attestationDataBuilder.authData(authData);

        attestationDataBuilder.authDataLength(authData.length);

        byte[] rpIdHash = Arrays.copyOfRange(authData,0,32);

        if(!Arrays.equals(DigestUtils.sha256(webAuthnConfig.getRelyingPartyId()),rpIdHash)){
            throw new AuthenticatorDataException();
        }
        attestationDataBuilder.rpidHash(rpIdHash);

        int flagBuf = authData[32];
        int up = flagBuf & 0X01;
        int uv = flagBuf & 0X04;
        int at = flagBuf & 0X40;
        int ed = flagBuf & 0x80;

        if(USER_PRESENT != up || USER_VERIFIED != 1){
            throw new AttestationException();
        }
        attestationDataBuilder.up(up);
        attestationDataBuilder.uv(uv);
        attestationDataBuilder.at(at);
        attestationDataBuilder.ed(ed);

        String counterBuffer= new String(Arrays.copyOfRange(authData,33,37));
        int index = 0;
        byte[] aaguid = Arrays.copyOfRange(authData,37, 53);
        attestationDataBuilder.aaguid(aaguid);

        if(authenticatorOperationType.equals(AuthenticatorOperationType.MAKE_CREDENTIAL)) {

            byte[] credIdLen = Arrays.copyOfRange(authData, 53, 55);
            attestationDataBuilder.credentialIdLength(credIdLen);

            int credIdOffset = (credIdLen[0] & 0XFF) << 8 |credIdLen[1] & 0xFF ;

            byte[] credentialId = Arrays.copyOfRange(authData, 55, 55 + credIdOffset);

            attestationDataBuilder.credentialIdLengthValue(credIdOffset);
            attestationDataBuilder.credentialId(credentialId);
            byte[] pk = Arrays.copyOfRange(authData, 55 + credIdOffset, authData.length);
            attestationDataBuilder.credentialPublicKey(pk);
        }
        return attestationDataBuilder.build();

    }
}

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

package org.orquanet.webauthn.webauthn.attestation.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.orquanet.webauthn.webauthn.attestation.data.AttestationObject;
import org.orquanet.webauthn.webauthn.attestation.model.Attestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorData;
import org.orquanet.webauthn.webauthn.attestation.model.fido.Fido2fAuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.packed.PackedAuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.data.AuthenticatorAttestationResponseWrapper;
import org.orquanet.webauthn.webauthn.attestation.constant.AttestationStatementFormat;
import org.orquanet.webauthn.webauthn.common.authdata.AuthenticatorDataReader;

import java.util.*;

public class AuthenticatorAttestationReader {

    private AuthenticatorDataReader authenticatorDataReader;
    private ObjectMapper cborMapper;

    public AuthenticatorAttestationReader(final AuthenticatorDataReader authenticatorDataReader) {
        this.authenticatorDataReader = authenticatorDataReader;
        this.cborMapper = new ObjectMapper(new CBORFactory());
    }




    public AuthenticatorAttestation read(AuthenticatorAttestationResponseWrapper authenticatorAttestationResponse) throws Exception {

        byte[] attestationBytes = Base64.getDecoder().decode(authenticatorAttestationResponse.getResponse().getAttestationObject());
        //ObjectMapper o = new ObjectMapper(new CBORFactory());
        JsonNode n = cborMapper.readTree(attestationBytes);
        logBor(n);
        Attestation attestation = this.read(attestationBytes);

        String clientDataJSON = authenticatorAttestationResponse.getResponse().getClientDataJSON();

        AuthenticatorAttestation authenticatorAttestation = null;

        AttestationStatementFormat attestationStatementFormat = attestation.getFmtEnum();

        switch (attestationStatementFormat) {
            case FIDOU2F:
                authenticatorAttestation = Fido2fAuthenticatorAttestation.builder().attestation(attestation).clientDataJSON(clientDataJSON).build();
                break;
            case PACKED:
                authenticatorAttestation = PackedAuthenticatorAttestation.builder().attestation(attestation).clientDataJSON(clientDataJSON).build();
                break;
        }
        return authenticatorAttestation;
    }

    public Attestation read(AttestationObject attestationObject) {
        AuthenticatorData authenticatorData = authenticatorData(attestationObject);
        String signature = attestationObject.getAttestationStatement().getSig();
        AttestationStatementFormat fmtEnum = attestationObject.getFmt();
        List<byte[]> x5c = attestationObject.getAttestationStatement().getX5c();

        Attestation attestation = Attestation.builder()
                .authenticatorData(authenticatorData)
                .signature(signature)

                .fmtEnum(fmtEnum)
                .x5c(Optional.ofNullable(x5c))
                .build();

        return attestation;
    }

    public Attestation read(byte[] attestationObjectBytesArray) throws Exception {
        //ObjectMapper mapper = new ObjectMapper(new CBORFactory());
        AttestationObject attestationObject = cborMapper.readValue(attestationObjectBytesArray, AttestationObject.class);
        return this.read(attestationObject);
    }

    private AuthenticatorData authenticatorData(AttestationObject attestationObject) {
        return authenticatorDataReader.read(attestationObject);
    }

    private void logBor(JsonNode n) {
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = n.fields();

        while (fieldsIterator.hasNext()) {

            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
        }
    }

}

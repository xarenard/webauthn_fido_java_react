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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyType;
import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;
import org.orquanet.webauthn.webauthn.attestation.constant.AttestationStatementFormat;
import org.orquanet.webauthn.webauthn.attestation.data.AttestationObject;
import org.orquanet.webauthn.webauthn.attestation.data.AuthenticatorAttestationResponseWrapper;
import org.orquanet.webauthn.webauthn.attestation.exception.AttestationFormatException;
import org.orquanet.webauthn.webauthn.attestation.model.Attestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorData;
import org.orquanet.webauthn.webauthn.attestation.model.fido.Fido2fAuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.packed.PackedAuthenticatorAttestationSupplier;
import org.orquanet.webauthn.webauthn.attestation.model.tpm.TPMAuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.tpm.TPMCertInfo;
import org.orquanet.webauthn.webauthn.attestation.model.tpm.TPMPubArea;
import org.orquanet.webauthn.webauthn.common.authdata.AuthenticatorDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class AuthenticatorAttestationReader {

    private static Logger LOGGER = LoggerFactory.getLogger(AuthenticatorAttestationReader.class);

    private AuthenticatorDataReader authenticatorDataReader;
    private ObjectMapper cborMapper;
    private Map<Integer, CoseAlgorithm> coseAlgorithms;

    public AuthenticatorAttestationReader(final AuthenticatorDataReader authenticatorDataReader,Map<Integer,CoseAlgorithm> coseAlgorithms) {
        this.authenticatorDataReader = authenticatorDataReader;
        this.cborMapper = new ObjectMapper(new CBORFactory());
        this.coseAlgorithms = coseAlgorithms;
    }

    public AuthenticatorAttestation readAttestation(AuthenticatorAttestationResponseWrapper authenticatorAttestationResponse) throws Exception {

        byte[] attestationBytes = Base64.getDecoder().decode(authenticatorAttestationResponse.getResponse().getAttestationObject());
        JsonNode n = cborMapper.readTree(attestationBytes);
        logBor(n);
        String clientDataJSON = authenticatorAttestationResponse.getResponse().getClientDataJSON();
        Attestation attestation = this.readAttestation(attestationBytes,clientDataJSON);

        AuthenticatorAttestation authenticatorAttestation = null;
        AttestationStatementFormat attestationStatementFormat = attestation.getFmtEnum();
        switch (attestationStatementFormat) {
            case FIDOU2F:
                authenticatorAttestation = Fido2fAuthenticatorAttestation.builder()
                        .attestation(attestation)
                        .clientDataJSON(clientDataJSON)
                        .build();
                break;
            case PACKED:
                authenticatorAttestation = PackedAuthenticatorAttestationSupplier.builder()
                        .attestation(attestation)
                        .clientDataJson(clientDataJSON)
                        .build()
                        .get();
                //authenticatorAttestation = PackedAuthenticatorSelfAttestation.builder().attestation(attestation).clientDataJSON(clientDataJSON).build();
                break;
            case TPM:
                authenticatorAttestation = TPMAuthenticatorAttestation.builder()
                        .attestation(attestation)
                        .clientDataJSON(clientDataJSON)
                        .build();
        }
        return authenticatorAttestation;
    }

    private Attestation readAttestation(AttestationObject attestationObject, String clientDataJson) {
        AuthenticatorData authenticatorData = authenticatorData(attestationObject);
        String signature = attestationObject.getAttestationStatement().getSig();
        AttestationStatementFormat fmtEnum = attestationObject.getFmt();
        List<byte[]> x5c = attestationObject.getAttestationStatement().getX5c();
        int coseAlgorithmValue = attestationObject.getAttestationStatement().getAlg();
        System.out.println("------" + coseAlgorithmValue);
        CoseAlgorithm coseAlgorithm = coseAlgorithms.get(coseAlgorithmValue);
        if(coseAlgorithm == null) {
            throw new AttestationFormatException("Invalid Argument");
        }
        // TPM initial values
        Optional<String> versionOptional = Optional.ofNullable(attestationObject.getAttestationStatement().getVer());
        Optional<TPMPubArea> pubAreaOptional = Optional.empty();
        Optional<TPMCertInfo> certInfoOptional = Optional.empty();

        if(fmtEnum.equals(AttestationStatementFormat.TPM)){
            //pubarea
            String pubAreaRawString = attestationObject.getAttestationStatement().getPubArea();
            TPMPubArea pubArea = readPubArea(pubAreaRawString);
            pubAreaOptional = Optional.of(pubArea);


            //certinfo
            String certInfoRawString = attestationObject.getAttestationStatement().getCertInfo();
            TPMCertInfo certInfo = readCertInfo(certInfoRawString, clientDataJson,authenticatorData,pubArea,coseAlgorithm);
            certInfoOptional = Optional.of(certInfo);
        }

        //attestationObject.getAttestationStatement()
        return Attestation.builder()
                .authenticatorData(authenticatorData)
                .signature(signature)
                .pubArea(pubAreaOptional)
                .version(versionOptional)
                .fmtEnum(fmtEnum)
                .certInfo(certInfoOptional)
                .x5c(Optional.ofNullable(x5c))
                .coseAlgorithm(coseAlgorithm)
                .keyType(KeyType.valueOf(coseAlgorithm.getKeyType()))
                .build();
    }

    private TPMPubArea readPubArea(final String pubArea){
        byte[] pubAreaBytes = Base64.getDecoder().decode(pubArea);

        //type - 2 bytes
        byte[] typeBytes = Arrays.copyOfRange(pubAreaBytes,0,2);
        int type = (typeBytes[0] & 0XFF) << 8 | typeBytes[1] &0XFF;

        //alg name - 2 bytes
        byte[] algorithmBytes = Arrays.copyOfRange(pubAreaBytes,2,4);
        int algorithm = (algorithmBytes[0] &0XFF) << 8 | algorithmBytes[1] &0XFF;



        // TODO
        // object attributes - 4 bytes
       /* byte[] objectAttribute = Arrays.copyOfRange(pubAreaBytes,4,8);
        int objectAttributeInt= (objectAttribute[0] & 0XFF) << 24 | (objectAttribute[1]  &0XFF) << 16  | (objectAttribute[2] & 0XFF) << 8  | objectAttribute[3] & 0xFF ;
        int fixedTPM = objectAttributeInt & 0X01;
        int stClear = objectAttributeInt & 0x02;
        int fixedParent = objectAttributeInt & 0x08;
        int sensitiveDataOrigin = objectAttributeInt & 0X10; //16
        int userWithAuth = objectAttributeInt & 0x20; //32
        */

        // authpolicy length - 2 bytes
        byte[] authPolicyLengthBytes = Arrays.copyOfRange(pubAreaBytes,8,10);
        int authPolicyLength = (authPolicyLengthBytes[0] & 0XFF) << 8 | (authPolicyLengthBytes[1] & 0XFF);
        int authPolicyOffset = 10 + authPolicyLength;

        // authpolicy
        byte[]  authPolicy = Arrays.copyOfRange(pubAreaBytes,10, authPolicyOffset );

        // parameters
        byte[] symetric = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset, authPolicyOffset + 2);
        byte[] scheme = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset + 2, authPolicyOffset + 4);
        int algOffSet = authPolicyOffset + 8;
        TPMPubArea.TPMAlg tpmAlg = null;
        if(type == 1) {
            byte[] keyBytes = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset + 4, authPolicyOffset + 6);
            byte[] exponent = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset + 6, authPolicyOffset + 10);
            tpmAlg = new TPMPubArea.TPMAlgRSA(symetric, scheme, keyBytes, exponent);
            algOffSet += 2;
            //EC
        } else if(type == 23){
            byte[] curveId = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset + 4, authPolicyOffset + 6);
            byte[] kdf = Arrays.copyOfRange(pubAreaBytes, authPolicyOffset + 6, authPolicyOffset + 8);
            tpmAlg = new TPMPubArea.TPMAlgECC(symetric,scheme,curveId,kdf);
        }

        byte[] uniqueLengthByte= Arrays.copyOfRange(pubAreaBytes,algOffSet, algOffSet + 2);
        int uniqueLength = (uniqueLengthByte[0] & 0XFF) << 8 | uniqueLengthByte[1] & 0XFF;

        int uniqueStartIndex = algOffSet + 2;
        byte[] unique = Arrays.copyOfRange(pubAreaBytes,uniqueStartIndex,uniqueStartIndex + uniqueLength);

        if(pubAreaBytes.length != uniqueStartIndex + uniqueLength){
            throw new AttestationFormatException("pubarea: invalid length");
        }

        return TPMPubArea.builder()
                .rawPubArea(pubAreaBytes)
                .type(type)
                .unique(unique)
                .authPolicy(authPolicy)
                .algorithm(algorithm)
                .alg(tpmAlg)
                .build();
    }

    private TPMCertInfo readCertInfo(final String certInfo,String clientDataJson,AuthenticatorData authenticatorData,TPMPubArea pubArea,CoseAlgorithm coseAlgorithm) {
        byte[] certInfoBytes = Base64.getDecoder().decode(certInfo);

        // Verify that magic is set to TPM_GENERATED_VALUE.
        byte[] magic = Arrays.copyOfRange(certInfoBytes,0,4);
        String magicHex = Hex.encodeHexString(magic);

        if(! "FF544347".equalsIgnoreCase(magicHex)){
            String errorMessage = String.format("Invalid magic number: %s",magicHex);
            LOGGER.error(errorMessage);
            throw new AttestationFormatException(errorMessage);
        }

        // Verify that type is set to TPM_ST_ATTEST_CERTIFY
        byte[] type= Arrays.copyOfRange(certInfoBytes,4,6);
        String typeHex = Hex.encodeHexString(type);
        if(!"8017".equalsIgnoreCase(typeHex)){
            String errorMessage = String.format("Invalid type: %s",typeHex);
            LOGGER.error(errorMessage);
            throw new AttestationFormatException(errorMessage);
        }

        // qualifiersigner
        byte[] qualifiedSignerLengthBytes  = Arrays.copyOfRange(certInfoBytes,6,8);
        int qualifiedSignerLength = (qualifiedSignerLengthBytes[0] & 0XFF) << 8 |qualifiedSignerLengthBytes[1] & 0xFF ;

        // extradata length
        byte[] extraDataLength  = Arrays.copyOfRange(certInfoBytes,8 + qualifiedSignerLength,8+qualifiedSignerLength +2);
        int extraDaTaInt = (extraDataLength[0] & 0XFF) << 8 |extraDataLength[1] & 0xFF ;
        int extraDataOffset = 10 + qualifiedSignerLength + extraDaTaInt;

        //extra data
        byte[] extraData = Arrays.copyOfRange(certInfoBytes,10 +qualifiedSignerLength,extraDataOffset);




        verifyExtraData(clientDataJson, authenticatorData, extraData,coseAlgorithm);

        int clockOffset = 17 + extraDataOffset;
        //byte[] firmwareversion = Arrays.copyOfRange(certInfoBytes,clockOffset,clockOffset + 8);
        int firmwareVersionOffset = clockOffset + 8;

        // attested name
        byte[] attestedLengthBytes = Arrays.copyOfRange(certInfoBytes,firmwareVersionOffset,firmwareVersionOffset + 2);
        int attestedNameLength = (attestedLengthBytes[0] & 0XFF) << 8 |attestedLengthBytes[1] & 0xFF ;
        byte[] attestedName = Arrays.copyOfRange(certInfoBytes,firmwareVersionOffset + 2,firmwareVersionOffset + 2 + attestedNameLength);

        // name alg
        byte[] nameAlgBytes = {attestedName[0], attestedName[1]};
       // int nameAlg = (nameAlgBytes[0] & 0XFF) << 8 |nameAlgBytes[1] & 0xFF ;
        int attestedNameOffset = firmwareVersionOffset + 2 + attestedNameLength;

        // attested qualifiedname
        byte[] attestedQualifiedNameLengthBytes = Arrays.copyOfRange(certInfoBytes, attestedNameOffset, attestedNameOffset + 2);
        int attestedNameQualifiedLength = (attestedQualifiedNameLengthBytes[0] & 0XFF) << 8 |attestedQualifiedNameLengthBytes[1] & 0xFF ;
       // byte[] attestedQualifiedName = Arrays.copyOfRange(certInfoBytes,attestedNameOffset + 2,attestedNameOffset + 2 + attestedNameQualifiedLength);

        //verify attestedName
        byte[] hashpubArea = DigestUtils.sha256(pubArea.getRawPubArea());

        String attestedNameHexComputed = Hex.encodeHexString(nameAlgBytes) + Hex.encodeHexString(hashpubArea);
        String attestedNameHex = Hex.encodeHexString(attestedName);
        LOGGER.debug(String.format("%s:%s",attestedNameHex,attestedNameHexComputed));
        if(!attestedNameHex.equals(attestedNameHexComputed)){
            String errorMessage = "Invalid attestedName";
            LOGGER.error(errorMessage);
            throw new AttestationFormatException(errorMessage);
        }

        int endParsingOffset = attestedNameOffset + attestedNameQualifiedLength + 2;
        if(endParsingOffset != certInfoBytes.length){
            LOGGER.error(String.format("certinfosize:%s - endparsingsize: %s",certInfoBytes.length,endParsingOffset));
            throw new AttestationFormatException("Invalid certinfo size");
        }

        return TPMCertInfo.builder()
                .magic(magicHex)
                .type(typeHex)
                .extraData(extraData)
                .attestedName(attestedNameHex)
                .rawCertInfo(certInfoBytes)
                .build();
    }

    private void verifyExtraData(final String clientDataJson, final AuthenticatorData authenticatorData, byte[] extraData,CoseAlgorithm coseAlgorithm) {

        byte[] clientDataRaw = Base64.getDecoder().decode(clientDataJson);
        byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);

       // String algorithms = coseAlgorithms.get()
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(authenticatorData.getAuthData());
            out.write(clientDataHash);
            out.close();
            byte[] extraDataHash = DigestUtils.getDigest(coseAlgorithm.getMessageDigestAlgorithm()).digest(out.toByteArray());

            String extraDataHashHex = Hex.encodeHexString(extraDataHash);
            String extraDataHex = Hex.encodeHexString(extraData);

            LOGGER.debug(String.format("%s - %s",extraDataHashHex, extraDataHex));
            if(!extraDataHashHex.equalsIgnoreCase(extraDataHex)){
                LOGGER.error(String.format("Invalid extradata: Received %s, expected %s",extraDataHashHex,extraDataHex));
                throw new AttestationFormatException("Invalid extradata");
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage());
            throw new AttestationFormatException("Invalid extradata");
        }
    }

    private Attestation readAttestation(byte[] attestationObjectBytesArray, String clientDataJson) throws Exception {
        //ObjectMapper mapper = new ObjectMapper(new CBORFactory());
        AttestationObject attestationObject = cborMapper.readValue(attestationObjectBytesArray, AttestationObject.class);
        return this.readAttestation(attestationObject,clientDataJson);
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

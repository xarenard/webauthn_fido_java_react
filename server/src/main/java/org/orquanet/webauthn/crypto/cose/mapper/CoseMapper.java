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

package org.orquanet.webauthn.crypto.cose.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.KeyType;
import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;
import org.orquanet.webauthn.crypto.cose.ec.constant.ECCurve;
import org.orquanet.webauthn.crypto.cose.exception.CoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.EnumSet.allOf;

/**
 * https://www.iana.org/assignments/cose/cose.xhtml
 */
public class CoseMapper {

    private static final String KTY_KEY = "1";
    private static final String ALG_KEY = "-1";
    private static final String X_POINT = "-2";
    private static final String Y_POINT = "-3";
    private static final String N = "-1";
    private static final String E = "-2";


    private static final Logger LOGGER = LoggerFactory.getLogger(CoseMapper.class);
    private ObjectMapper mapper = new ObjectMapper(new CBORFactory());
    private Map<Integer, ECCurve> ecCurves = new HashMap();
    private Map<Integer, CoseAlgorithm> coseAlgorithms;

    public CoseMapper() {
        ecCurves.putAll(allOf(ECCurve.class)
                .stream()
                .collect(Collectors.toMap(ECCurve::coseId, Function.identity())));

        coseAlgorithms = allOf(CoseAlgorithm.class)
                .stream()
                .map(ca -> new AbstractMap.SimpleEntry<Integer,CoseAlgorithm>(ca.getValue(), ca))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    public KeyInfo keyInfo(byte[] coseKey) {
        KeyInfo keyInfo = null;
        Map<String, ?> coseKV = toKV(coseKey);

        if(!coseKV.containsKey(KTY_KEY)){
            throw new CoseException("Missing Kty Key");
        }
        Integer ktyId = (Integer) coseKV.get(KTY_KEY);
        Optional<KeyType> keytypeOptional = KeyType.fromInt(ktyId);
        KeyType keyType = keytypeOptional.orElseThrow(CoseException::new);

        switch (keyType) {
            case RESERVED:
                throw new CoseException("Reserved not supported");
            case OPK:
                throw new CoseException("OPK not supported");
            case ECDSA:
                Integer alg = (Integer) coseKV.get(ALG_KEY);
                if(! ecCurves.containsKey(alg)) {
                    throw new CoseException("Invalid Cose Algorithm");
                }
                byte[] x = (byte[]) coseKV.get(X_POINT);
                byte[] y = (byte[]) coseKV.get(Y_POINT);

                PublicKey publicKey = this.ecPublicKey(ecCurves.get(alg), x, y);
                ECCurve ecCurve = ecCurves.get(alg);
                CoseAlgorithm coseAlgorithm = ecCurveToCoseAlgorithm(ecCurve);
                keyInfo = KeyInfo.builder()
                        .publicKey(publicKey)
                        .coseAlgorithm(coseAlgorithm)
                        .build();
                break;
            case RSA:
                byte[] modulus = (byte[]) coseKV.get(N);
                byte[] exponent = (byte[]) coseKV.get(E);
                PublicKey rsaPublicKey = rsaPublicKey(modulus,exponent);
                keyInfo = KeyInfo.builder()
                        .publicKey(rsaPublicKey)
                        // hard coded
                        .coseAlgorithm(CoseAlgorithm.RS256)
                        .build();
        }
        return keyInfo;
    }

    private PublicKey rsaPublicKey(byte[] modulus, byte[] exponent){
        PublicKey publicKey = null;
        try {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            publicKey = factory.generatePublic(spec);
        } catch(InvalidKeySpecException | NoSuchAlgorithmException e){
            LOGGER.error(e.getMessage());
            throw new CoseException(e);
        }
        return publicKey;
    }
    private Map<String, ?> toKV(byte[] cosePublicKey) {
        Map<String, ?> coseKV = null;
        try {
            coseKV = mapper.readValue(cosePublicKey, Map.class);

            if (LOGGER.isDebugEnabled()) {
                coseKV.forEach((k, v) -> {
                    LOGGER.debug(String.format("%s: %s", k, v));
                });
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new CoseException(e);
        }
        return coseKV;
    }

    private PublicKey ecPublicKey (ECCurve curve, byte[] x, byte[] y) {

        if(curve == null || x == null || y == null){
            throw new IllegalArgumentException("Invalid Argument");
        }

        PublicKey key;
        try {
            BigInteger xPoint = new BigInteger(x);
            BigInteger yPoint = new BigInteger(y);

            ECPoint ecPoint = new ECPoint(xPoint, yPoint);
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "BC");
            parameters.init(new ECGenParameterSpec(curve.curveName()));
            ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ecPoint, ecParameters);
            key = KeyFactory.getInstance("EC").generatePublic(pubKeySpec);
        } catch(NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException | InvalidKeySpecException e){
            LOGGER.error(e.getMessage());
            throw new CoseException(e);
        }
        return key;
    }

    private CoseAlgorithm ecCurveToCoseAlgorithm(ECCurve ecCurve){
        CoseAlgorithm coseAlgorithm;
        switch (ecCurve){
            case P256:
                coseAlgorithm = CoseAlgorithm.ES256;
                break;
            case P384:
                coseAlgorithm = CoseAlgorithm.ES384;
                break;
            case P521:
                coseAlgorithm = CoseAlgorithm.ES512;
                break;
            default:
                throw new CoseException("Invalid EC Curve");

        }
        return  coseAlgorithm;
    }
}

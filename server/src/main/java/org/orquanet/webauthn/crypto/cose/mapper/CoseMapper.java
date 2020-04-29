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
import org.orquanet.webauthn.crypto.cose.ec.constant.ECCurve;
import org.orquanet.webauthn.crypto.cose.exception.CoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.EnumSet.allOf;

public class CoseMapper {

    public static final String KTY_KEY = "1";
    public static final String ALG_KEY = "-1";
    public static final String X_POINT = "-2";
    public static final String Y_POINT = "-3";

    public static final Logger LOGGER = LoggerFactory.getLogger(CoseMapper.class);

    private ObjectMapper mapper = new ObjectMapper(new CBORFactory());

    private Map<Integer, ECCurve> ecCurves = new HashMap();


    public CoseMapper() {
        ecCurves.putAll(allOf(ECCurve.class)
                .stream()
                .collect(Collectors.toMap(ECCurve::coseId, Function.identity())));
    }


    public KeyInfo keyInfo(byte[] coseKey) {

        KeyInfo keyInfo = null;

        Map<String, ?> coseKV = toKV(coseKey);

        if(!coseKV.containsKey(KTY_KEY)){
            throw new CoseException("Missing Kty Key");
        }

        Integer ktyId = (Integer) coseKV.get(KTY_KEY);

        switch (ktyId) {
            case 1:
                throw new CoseException("OPK not supported");
            case 2:
                Integer alg = (Integer) coseKV.get(ALG_KEY);
                if(! ecCurves.containsKey(alg)){

                    ecCurves.forEach((k,v) -> {
                        System.out.println(k + ":"+v);
                    });
                    throw new CoseException("Invalid Cose Algorithm");
                }
                byte[] x = (byte[]) coseKV.get(X_POINT);
                byte[] y = (byte[]) coseKV.get(Y_POINT);

                PublicKey publicKey = this.read(ecCurves.get(alg), x, y);
                keyInfo = KeyInfo.builder().publicKey(publicKey).algorithm(ecCurves.get(alg)).build();
                break;
            case 3:
                throw new CoseException("RSA not supported");
        }
        return keyInfo;
    }

    public Map<String, ?> toKV(byte[] cosePublicKey) {
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

    public PublicKey read (ECCurve curve, byte[] x, byte[] y) {

        if(curve == null || x == null || y == null){
            throw new IllegalArgumentException("Invalid Argument");
        }

        PublicKey key= null;

        try {
            BigInteger xPoint = new BigInteger(x);
            BigInteger yPoint = new BigInteger(y);

            ECPoint ecPoint = new ECPoint(xPoint, yPoint);
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "SunEC");
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
}

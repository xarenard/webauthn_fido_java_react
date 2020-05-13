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

package org.orquanet.webauthn.crypto.cose.ec.constant;

public enum ECCurve{

    P256(1,"secp256r1","SHA256withECDSA"),
    P384(2,"secp384r1","SHA384withECDSA"),
    P521(3,"secp521r1","SHA512withECDSA");

    private String curveName;
    private int coseId;
    private String signatureAlgorithm;

    ECCurve(int coseId, String curveName, String signatureAlgorithm){
        this.coseId = coseId;
        this.curveName = curveName;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public int coseId(){
        return this.coseId;
    }

    public String curveName(){
        return this.curveName;
    }

    public String signatureAlgorithm() {
        return this.signatureAlgorithm;
    }
}

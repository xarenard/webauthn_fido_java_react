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

package org.orquanet.webauthn.crypto.signature;

import org.orquanet.webauthn.crypto.KeyInfo;

import java.security.Signature;

public class SignatureVerifier {


    public boolean validate(final byte[] message, final byte[] signature, KeyInfo keyInfo) throws Exception {
        //hard coded for the moment
        Signature signatureObject = Signature.getInstance("SHA256withECDSA");
        signatureObject.initVerify(keyInfo.getPublicKey());
        signatureObject.update(message);
        return signatureObject.verify(signature);
    }


}

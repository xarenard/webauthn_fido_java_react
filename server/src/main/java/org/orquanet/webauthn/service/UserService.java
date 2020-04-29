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

package org.orquanet.webauthn.service;

import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.repository.model.FidoCredential;
import org.orquanet.webauthn.repository.model.FidoUser;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class UserService {

    private UserRepository repository;

    public UserService(UserRepository repository){
        this.repository = repository;
    }

    public Optional<FidoUser> findUser(String email){
        return this.repository.findFidoUserDetailsByMail(email);
    }

    public void saveCredential(AuthenticatorAttestation authenticatorAttestation,FidoUser fidoUser){

       KeyInfo keyInfo = authenticatorAttestation.getKeyInfo();
        FidoCredential fidoCredential = FidoCredential.builder().publicKey(keyInfo.getPublicKey().getEncoded())
                .credentialId(Base64.getEncoder().encodeToString(authenticatorAttestation.getAttestation().getAuthenticatorData().getCredentialId()))
                .created(new Date())
                .fidoUser(fidoUser)
                .build();
        this.repository.save(fidoCredential);
    }
}

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

import org.orquanet.webauthn.repository.CredentialRepository;
import org.orquanet.webauthn.repository.model.FidoCredential;

import java.util.Collection;
import java.util.Optional;

public class CredentialService {

    private CredentialRepository credentialRepository;

    public CredentialService(CredentialRepository credentialRepository){
        this.credentialRepository = credentialRepository;
    }

    public Optional<FidoCredential> credential(final String credentialId, final String fidoUserId){
        return credentialRepository.findCredential(credentialId,fidoUserId);
    }

    public Collection<FidoCredential> credentials( final String fidoUserId){

        return credentialRepository.findCredentials(fidoUserId);

    }

    public Collection<String> credentialIds( final String fidoUserId){

        return credentialRepository.findCredentialIds(fidoUserId);

    }
}

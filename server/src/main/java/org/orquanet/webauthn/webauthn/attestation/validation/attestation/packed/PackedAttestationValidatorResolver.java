/*
 * Copyright 2018 the original author or authors.
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

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed;

import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.full.PackedFullAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.self.PackedDerogateAttestationValidator;

public class PackedAttestationValidatorResolver {

    public PackedAttestationValidator resolve(AuthenticatorAttestation authenticatorAttestation){
        PackedAttestationValidator packedAttestationValidator = null;

        if(authenticatorAttestation.getAttestation().getX5c().isPresent()){
            packedAttestationValidator = new PackedFullAttestationValidator();
        } else {
            packedAttestationValidator = new PackedDerogateAttestationValidator();
        }

        return packedAttestationValidator;
    }
}

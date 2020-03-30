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

package org.orquanet.webauthn.webauthn.attestation.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.orquanet.webauthn.webauthn.attestation.constant.AuthenticatorAttachment;
import org.orquanet.webauthn.webauthn.attestation.constant.UserVerificationRequirement;

@Builder
@Getter
public class AuthenticatorSelectionCriteria {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("authenticatorAttachment")
    private AuthenticatorAttachment authenticatorAttachment;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("userVerification")
    private UserVerificationRequirement userVerificationRequirement;

}

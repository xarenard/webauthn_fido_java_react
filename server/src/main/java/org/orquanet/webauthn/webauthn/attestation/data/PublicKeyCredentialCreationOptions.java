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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.orquanet.webauthn.webauthn.attestation.constant.AttestationConveyancePreference;
import org.orquanet.webauthn.webauthn.common.data.PublicKeyCredentialDescriptor;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(value="publicKey")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public final class PublicKeyCredentialCreationOptions {

    @NotNull
    private String challenge;

    @JsonProperty(value = "rp", required = true)
    private RelyingParty relyingParty;

    private User user;
    @JsonProperty(value = "pubKeyCredParams")
    private Collection<PubKeyCredParams> pubKeyCredParamData;

    @JsonProperty(value = "timout", defaultValue = "60000")
    private int timeOut = 60000;

    @JsonProperty(value = "attestation")
    private AttestationConveyancePreference attestationConveyancePreference;

    @JsonProperty(value = "excludeCredentials")
    public Set<PublicKeyCredentialDescriptor> excludeCredentials;

    @JsonProperty(value="authenticatorSelection")
    private AuthenticatorSelectionCriteria authenticatorSelectionCriteria;
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }
}




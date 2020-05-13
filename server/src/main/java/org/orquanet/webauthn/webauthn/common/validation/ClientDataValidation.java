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

package org.orquanet.webauthn.webauthn.common.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class ClientDataValidation {

    public static final String CLIENT_DATA_TYPE_KEY = "type";
    public static final String CLIENT_DATA_ORIGIN_KEY = "origin";
    public static final String CLIENT_DATA_CHALLENGE_KEY="challenge";

    private ObjectMapper objectMapper = new ObjectMapper();

    private WebAuthnConfig webAuthnConfig;

    protected ClientDataValidation(WebAuthnConfig webAuthnConfig){
        this.webAuthnConfig = webAuthnConfig;
    }

    public WebAuthnConfig getWebAuthnConfig() {
        return webAuthnConfig;
    }

    protected Map<String,String> getClientDataAsMap(String clientDataJSON){
        if (clientDataJSON == null) {
            throw new IllegalArgumentException();
        }

        try {
            return objectMapper.readValue(Base64.getUrlDecoder().decode(clientDataJSON), Map.class);
        } catch (IOException e) {
            throw new ClientDataValidationException(e);
        }

    }
}

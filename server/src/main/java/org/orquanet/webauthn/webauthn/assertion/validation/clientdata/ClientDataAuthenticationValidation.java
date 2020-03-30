package org.orquanet.webauthn.webauthn.assertion.validation.clientdata;

import org.orquanet.webauthn.webauthn.assertion.exception.AuthenticationException;
import org.orquanet.webauthn.webauthn.attestation.exception.RegistrationException;
import org.orquanet.webauthn.webauthn.attestation.validation.clientdata.ClientDataRegistrationValidation;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;
import org.orquanet.webauthn.webauthn.common.validation.ClientDataValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class ClientDataAuthenticationValidation extends ClientDataValidation {

    private static final String CLIENT_DATA_GET_TYPE_VALUE = "webauthn.get";

    public static Logger LOGGER = LoggerFactory.getLogger(ClientDataRegistrationValidation.class);

    public ClientDataAuthenticationValidation(WebAuthnConfig webAuthnConfig) {
        super(webAuthnConfig);
    }

    public boolean validate(final String clientDataJSON, final String challenge) {
        if (clientDataJSON == null || challenge == null) {
            throw new RegistrationException();
        }

        boolean isValid;
        try {
            Map<String, String> clientData = getClientDataAsMap(clientDataJSON);

            if (!clientData.containsKey(CLIENT_DATA_CHALLENGE_KEY)
                    || !clientData.containsKey(CLIENT_DATA_TYPE_KEY)
                    || !clientData.containsKey(CLIENT_DATA_ORIGIN_KEY)
                    || !clientData.containsKey(CLIENT_DATA_ORIGIN_KEY)
                    || !clientData.containsKey(CLIENT_DATA_CHALLENGE_KEY)) {
                throw new RegistrationException("Invalid Client Data");
            }

            //3 Verify that the value of C.type is webauthn.create
            if (!CLIENT_DATA_GET_TYPE_VALUE.equals(clientData.get(CLIENT_DATA_TYPE_KEY))) {
                throw new RegistrationException("Invalid Client Data");
            }

            //4. Verify that the value of C.challenge matches the challenge that was sent to the authenticator in the create() call
            if (challenge.equals(clientData.get(CLIENT_DATA_CHALLENGE_KEY))) {
                LOGGER.error("Invalid challenge");
                throw new RegistrationException();
            }
            // TODO
            boolean allowedOriginsValidation = getWebAuthnConfig().getAllowedOriginsValidation();
            Set<String> allowedOrigins = getWebAuthnConfig().getAllowedOrigins();
            //5. Verify that the value of C.origin matches the Relying Party's origin
            if (allowedOriginsValidation && !allowedOrigins.contains(clientData.get(CLIENT_DATA_ORIGIN_KEY))) {
                LOGGER.error("Invalid origin");
                throw new RegistrationException();
            }

            //TODO
            //6. Verify that the value of C.tokenBinding.status matches the state of Token Binding for the TLS connection over
            // which the assertion was obtained. If Token Binding was used on that TLS connection, also verify that C.tokenBinding.id
            // matches the base64url encoding of the Token Binding ID for the connection

            isValid = Boolean.TRUE;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new AuthenticationException(e);
        }

        return isValid;
    }

}

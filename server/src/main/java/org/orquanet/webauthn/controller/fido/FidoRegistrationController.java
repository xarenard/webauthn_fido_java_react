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

package org.orquanet.webauthn.controller.fido;

import org.orquanet.webauthn.controller.session.WebauthnSession;
import org.orquanet.webauthn.controller.user.dto.UserDto;
import org.orquanet.webauthn.repository.model.FidoUser;
import org.orquanet.webauthn.service.UserService;
import org.orquanet.webauthn.webauthn.attestation.constant.AttestationConveyancePreference;
import org.orquanet.webauthn.webauthn.attestation.constant.PubKeyCredentialParamAlgorithms;
import org.orquanet.webauthn.webauthn.attestation.constant.UserVerificationRequirement;
import org.orquanet.webauthn.webauthn.attestation.data.*;
import org.orquanet.webauthn.webauthn.attestation.exception.RegistrationException;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.reader.AuthenticatorAttestationReader;
import org.orquanet.webauthn.webauthn.attestation.validation.AuthenticatorAttestationValidator;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;
import org.orquanet.webauthn.webauthn.common.data.PublicKeyCredentialDescriptor;
import org.orquanet.webauthn.webauthn.common.data.PublicKeyCredentialType;
import org.orquanet.webauthn.webauthn.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FidoRegistrationController  extends FidoController {

    private AuthenticatorAttestationReader authenticatorAttestationReader;
    private AuthenticatorAttestationValidator authenticatorAttestationValidator;
    private UserService userService;
    private WebAuthnConfig webAuthnConfig;

    private static final String REGISTRATION_SESSION_NAME = "registrationsession";

    public FidoRegistrationController(final AuthenticatorAttestationReader authenticatorAttestationReader,
                                      final AuthenticatorAttestationValidator authenticatorAttestationValidator,
                                      final UserService userService,
                                      final WebAuthnConfig webAuthnConfig) {
        this.authenticatorAttestationReader = authenticatorAttestationReader;
        this.authenticatorAttestationValidator = authenticatorAttestationValidator;
        this.userService = userService;
        this.webAuthnConfig = webAuthnConfig;
    }

    @CrossOrigin(origins = {"${webauthn.origins.allowed}"}, allowCredentials = "true", methods = RequestMethod.POST)
    @RequestMapping(path = "/registration/init", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<PublicKeyCredentialCreationOptions> register(@RequestBody UserDto userDto, HttpServletRequest request) {//@ModelAttribute("registrationsession") RegistrationSession registrationSession) {

        // should be login or identification instead
        Optional<FidoUser> fidoUserOptional = userService.findFidoUser(userDto.getEmail());
        FidoUser fidoUser = fidoUserOptional.orElseThrow(UserNotFoundException::new);
        Set<PublicKeyCredentialDescriptor> allowCredentials = fidoUser
                .getFidoCredentials()
                .stream()
                .map(c -> PublicKeyCredentialDescriptor
                        .builder()
                        .id(c.getCredentialId())
                                .type(PublicKeyCredentialType.PUBLIC_KEY.value())
                                .build())
                .collect(Collectors.toSet());

        // build the relying party
        RelyingParty relyingParty = RelyingParty.builder()
                .name(webAuthnConfig.getRelyingPartyName())
                .id(webAuthnConfig.getRelyingPartyId())
                .build();

        // build user
        User user = User.builder()
                .displayName(String.format("%s %s", fidoUser.getUser().getFirstName(), fidoUser.getUser().getLastName()))
                .name(fidoUser.getUser().getLastName())
                .id(fidoUser.getFidoId())
                .build();

        // build pubkeycredentialparamalgorithms
        PubKeyCredentialParamAlgorithms[] algorithms = PubKeyCredentialParamAlgorithms.values();
        Collection<PubKeyCredParams> pubkey = Arrays.stream(algorithms).map(alg -> PubKeyCredParams
                .builder()
                .type(PublicKeyCredentialType.PUBLIC_KEY.value())
                .pubKeyCredentialParamAlgorithms(alg)
                .build()).collect(Collectors.toList());


        byte[] challengeRaw = challenge();
        String challenge = Base64.getEncoder().encodeToString(challengeRaw);

        AuthenticatorSelectionCriteria authenticatorSelectionCriteria = AuthenticatorSelectionCriteria.builder()
                //.authenticatorAttachment(AuthenticatorAttachment.CROSSPLATFORM)
                .userVerificationRequirement(UserVerificationRequirement.DISCOURAGED)
                .build();

        // create response to send back
        PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions = PublicKeyCredentialCreationOptions
                .builder()
                .challenge(Base64.getEncoder().encodeToString(challengeRaw))
                .relyingParty(relyingParty)
                .pubKeyCredParamData(pubkey)
                .user(user)
                .excludeCredentials(allowCredentials)
                .authenticatorSelectionCriteria(authenticatorSelectionCriteria)
                .attestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                .timeOut(60000)
                .build();
        // store in session - could be redis,...
        initWebauthnSession(REGISTRATION_SESSION_NAME,request,challenge,fidoUser);
        return new ResponseEntity<>(publicKeyCredentialCreationOptions, HttpStatus.OK);

    }

    @CrossOrigin(origins = "${webauthn.origins.allowed}", allowCredentials = "true", methods = {RequestMethod.POST})
    @PostMapping(path = "/registration/final/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void registerFinal(@PathParam ("id") String id,@RequestBody AuthenticatorAttestationResponseWrapper authenticatorAttestationResponseWrapper, HttpServletRequest request) throws Exception {

        HttpSession session = request.getSession();
        WebauthnSession webauthnSession = (WebauthnSession) session.getAttribute(REGISTRATION_SESSION_NAME);
        session.invalidate();

        String fidoId = webauthnSession.getFidoUser().getFidoId();

        if(fidoId == null || fidoId.equals(id)){
            throw new RegistrationException("Invalid Fido User Id");
        }

        AuthenticatorAttestation authenticatorAttestation = authenticatorAttestationReader.readAttestation(authenticatorAttestationResponseWrapper);

        authenticatorAttestationValidator.validate(authenticatorAttestation, webauthnSession.getChallenge());
        userService.saveCredential(authenticatorAttestation, webauthnSession.getFidoUser());
    }

    private void initRegistrationSession(HttpServletRequest request, String challenge, FidoUser fidoUser) {
        WebauthnSession webauthnSession = new WebauthnSession();
        webauthnSession.setChallenge(challenge);
        webauthnSession.setFidoUser(fidoUser);
        HttpSession session = request.getSession(true);
        session.setAttribute(REGISTRATION_SESSION_NAME, webauthnSession);
    }
}

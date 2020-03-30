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
package org.orquanet.webauthn.config;

import org.orquanet.webauthn.repository.CredentialRepository;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.service.CredentialService;
import org.orquanet.webauthn.service.UserService;
import org.orquanet.webauthn.webauthn.assertion.reader.AuthenticatorAssertionReader;
import org.orquanet.webauthn.webauthn.assertion.validation.clientdata.ClientDataAuthenticationValidation;
import org.orquanet.webauthn.webauthn.assertion.validation.signature.AssertionSignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.reader.AuthenticatorAttestationReader;
import org.orquanet.webauthn.webauthn.attestation.validation.clientdata.ClientDataRegistrationValidation;
import org.orquanet.webauthn.webauthn.attestation.validation.signature.AttestationSignatureValidation;
import org.orquanet.webauthn.webauthn.attestation.validation.signature.fido2f.Fido2fAttestationSignatureValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.signature.packed.PackedAttestationSignatureValidator;
import org.orquanet.webauthn.webauthn.common.authdata.AuthenticatorDataReader;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Base64;
import java.util.Set;

@Configuration
@EnableJpaRepositories(basePackages = {"org.orquanet.webauthn.repository"})
@PropertySource("classpath:webauthn.properties")
public class FidoConfig {

    @Value("${webauthn.origins.allowed}")
    private Set<String> allowedOrigins;

    @Value("${webauthn.origins.allowed.enabled:true}")
    private Boolean allowedOriginsValidation;

    @Value("${webauthn.relying.party.id}")
    private String relyingPartyId;

    @Value("${webauthn.relying.party.name}")
    private String relyingPartyName;

    @Bean
    public UserService userService(UserRepository userRepository){
        return new UserService(userRepository);
    }

    @Bean
    public CredentialService credentialService(CredentialRepository credentialRepository){
        return new CredentialService(credentialRepository);
    }
    @Bean
    public ClientDataRegistrationValidation clientDataRegistrationValidator(){
        return new ClientDataRegistrationValidation(webAuthnConfig());
    }

    @Bean
    public ClientDataAuthenticationValidation clientDataAuthenticationValidator(){
        return new ClientDataAuthenticationValidation(webAuthnConfig());
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean(name="base64urldecoder")
    public Base64.Decoder base64UrlDecoder() {
        return Base64.getUrlDecoder();
    }

    @Bean(name="base64urlencoder")
    public Base64.Encoder base64UrlEncoder() {
        return Base64.getUrlEncoder();
    }

    @Bean
    public AttestationSignatureValidation attestationSignatureValidation(){
        return new AttestationSignatureValidation(fido2fAttestationSignatureValidator(),packedAttestationSignatureValidator());
    }

    @Bean
    public Fido2fAttestationSignatureValidator fido2fAttestationSignatureValidator(){
        return new Fido2fAttestationSignatureValidator();
    }

    @Bean
    public PackedAttestationSignatureValidator packedAttestationSignatureValidator() {
        return new PackedAttestationSignatureValidator();
    }

    @Bean
    public AssertionSignatureVerifier assertionSignatureValidation() {
        return new AssertionSignatureVerifier();
    }

    @Bean
    public AuthenticatorAttestationReader authenticatorAttestationReader(){
        return new AuthenticatorAttestationReader(authenticatorDataReader());
    }

    @Bean
    public AuthenticatorAssertionReader authenticatorAssertionReader(){
        return new AuthenticatorAssertionReader(authenticatorDataReader());
    }
    @Bean
    public AuthenticatorDataReader authenticatorDataReader(){
        return new AuthenticatorDataReader(webAuthnConfig());
    }

    @Bean
    public WebAuthnConfig webAuthnConfig(){
        return WebAuthnConfig
                .builder()
                .allowedOrigins(allowedOrigins)
                .allowedOriginsValidation(allowedOriginsValidation)
                .relyingPartyId(relyingPartyId)
                .relyingPartyName(relyingPartyName)
                .build();
    }
}

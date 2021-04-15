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

import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;
import org.orquanet.webauthn.repository.CredentialRepository;
import org.orquanet.webauthn.repository.FidoUserRepository;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.service.CredentialService;
import org.orquanet.webauthn.service.UserService;
import org.orquanet.webauthn.webauthn.assertion.reader.AuthenticatorAssertionReader;
import org.orquanet.webauthn.webauthn.assertion.validation.clientdata.ClientDataAuthenticationValidation;
import org.orquanet.webauthn.webauthn.assertion.validation.signature.AssertionSignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.reader.AuthenticatorAttestationReader;
import org.orquanet.webauthn.webauthn.attestation.validation.AuthenticatorAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.tpm.TPMAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.clientdata.ClientDataRegistrationValidation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.AttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.fido2f.Fido2fAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.PackedAttestationValidatorResolver;
import org.orquanet.webauthn.webauthn.common.authdata.AuthenticatorDataReader;
import org.orquanet.webauthn.webauthn.common.WebAuthnConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(TPMConfigurations.class)
@EnableJpaRepositories(basePackages = {"org.orquanet.webauthn.repository"})
@PropertySource({"classpath:webauthn.properties"})
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
    public UserService userService(FidoUserRepository fidoUserRepository, UserRepository userRepository){
        return new UserService(fidoUserRepository,userRepository);
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
    public AttestationValidator attestationValidator(){
        return new AttestationValidator(fido2fAttestationSignatureValidator(),packedAttestationValidatorResolver(),tpmAttestationValidator());
    }

    @Bean
    public Fido2fAttestationValidator fido2fAttestationSignatureValidator(){
        return new Fido2fAttestationValidator();
    }

    @Bean
    public AssertionSignatureVerifier assertionSignatureValidation() {
        return new AssertionSignatureVerifier(coseAlgorithms());
    }

    @Bean
    public AuthenticatorAttestationReader authenticatorAttestationReader(){
        return new AuthenticatorAttestationReader(authenticatorDataReader(),coseAlgorithms());
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

    @Bean
    public PackedAttestationValidatorResolver packedAttestationValidatorResolver(){
        return new PackedAttestationValidatorResolver();
    }
    @Bean
    public AuthenticatorAttestationValidator authenticatorAttestationValidator() {
        return new AuthenticatorAttestationValidator(clientDataRegistrationValidator(),attestationValidator());
    }

    @Bean
    public TPMAttestationValidator tpmAttestationValidator(){
        return new TPMAttestationValidator(tpmConfigurations());
    }

    @Bean
    public Map<Integer, CoseAlgorithm> coseAlgorithms(){
        return EnumSet.allOf(CoseAlgorithm.class)
                .stream()
                .map(ca -> new AbstractMap.SimpleEntry<Integer,CoseAlgorithm>(ca.getValue(), ca))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Bean
    public TPMConfigurations tpmConfigurations(){
        return new TPMConfigurations();
    }
}

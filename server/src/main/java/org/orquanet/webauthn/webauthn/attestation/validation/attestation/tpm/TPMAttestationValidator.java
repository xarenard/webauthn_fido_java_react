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

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.tpm;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.orquanet.webauthn.config.TPMConfigurations;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.crypto.utils.X509Utils;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.model.tpm.TPMCertInfo;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.X5cAttestationValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class TPMAttestationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPMAttestationValidator.class);

    private TPMConfigurations tpmConfigurations;

    private String TGC_KP_AIK_CERTIFICATE= "2.23.133.8.3";

    public TPMAttestationValidator(TPMConfigurations tpmConfigurations){
        this.tpmConfigurations = tpmConfigurations;
    }


    public void verify(AuthenticatorAttestation authenticatorAttestation)  {

        if (authenticatorAttestation == null) {
            throw new IllegalArgumentException();
        }

        try {
            Optional<List<byte[]>> certsOptional = authenticatorAttestation.getAttestation().getX5c();
            //If x5c is present, this indicates that the attestation type is not ECDAA
            // If ecdaaKeyId is present, then the attestation type is ECDAA
            if (!certsOptional.isPresent()) {
                    throw new AttestationValidationException("ECDAA not supported");
             } else {
                List<byte[]> certsBytes = certsOptional.get();
                X509Certificate certificate = X509Utils.x509CertificateFromBytesArray(certsBytes.get(0));

                //1.Verify the sig is a valid signature over certInfo using the attestation public key in aikCert with the algorithm specified in al
                String signature = authenticatorAttestation.getAttestation().getSignature();
                Optional<TPMCertInfo> certInfoOptional = authenticatorAttestation.getAttestation().getCertInfo();
                TPMCertInfo certInfo = certInfoOptional.orElseThrow(AttestationValidationException::new);
                byte[] rawCertInfo = certInfo.getRawCertInfo();
                SignatureVerifier signatureVerifier = new SignatureVerifier();
                CoseAlgorithm coseAlgorithm = authenticatorAttestation.getAttestation().getCoseAlgorithm();
                KeyInfo keyInfo = KeyInfo.builder()
                            .publicKey(certificate.getPublicKey())
                            .coseAlgorithm(coseAlgorithm)
                            .build();
                boolean validSignature = signatureVerifier.validate(rawCertInfo, Base64.getDecoder().decode(signature), keyInfo);
                if(!validSignature){
                    throw new AttestationValidationException("Invalid Signature");
                }

                //2.Verify that aikCert meets the requirements in §8.3.1 TPM Attestation Statement Certificate Requirements.
                // Version MUST be set to 3
                if (certificate.getVersion() != 3) {
                    throw new X5cAttestationValidationException("Invalid Certificate Version");
                }

                //2.2 Subject field MUST be set to empty.
                Principal dn = certificate.getSubjectDN();
                if(!"".equals(dn.getName().trim())) {
                    throw new X5cAttestationValidationException("Subject is empty");
                }

                // TO DO check expire
                Date notBefore = certificate.getNotBefore();
                Date notAfter = certificate.getNotAfter();

                //The Subject Alternative Name extension MUST be set as defined in [TPMv2-EK-Profile] section 3.2.9
                Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
                Map<String,String> manufacturersExtensionMap = altNames.stream().filter(item -> item.get(0) instanceof Integer && (Integer) item.get(0) == 4 )
                        .map(item -> new X500Name((String)item.get(1)))
                        .map(X500Name::getRDNs)
                        .flatMap(Arrays::stream)
                        .map(RDN::getFirst)
                        .map(x -> new AbstractMap.SimpleEntry<String,String>(x.getType().getId(),x.getValue().toString()))
                        .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));

                if(!manufacturersExtensionMap.containsKey("2.23.133.2.1") && "".equals(manufacturersExtensionMap.get("2.23.133.2.1"))){
                    throw new X5cAttestationValidationException("Invalid manufacturer extension");
                }

                //The Extended Key Usage extension MUST contain the "joint-iso-itu-t(2) internationalorganizations(23) 133 tcg-kp(8) tcg-kp-AIKCertificate(3)" OID.
                certificate.getExtendedKeyUsage().stream().filter(eku -> TGC_KP_AIK_CERTIFICATE.equals(eku)).findAny().orElseThrow(RuntimeException::new);

                // The Basic Constraints extension MUST have the CA component set to false
                int basicConstraints = certificate.getBasicConstraints();
                if(basicConstraints != -1){
                    throw new X5cAttestationValidationException("Invalid Basic Constraints.");
                }
            }
        }catch(Exception e){
            LOGGER.error(e.getMessage());
            throw new X5cAttestationValidationException(e);
        }
    }
}

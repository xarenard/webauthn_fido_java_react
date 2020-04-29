package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.self;

import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.PackedAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.X5cValidationException;

import java.security.cert.X509Certificate;

public class PackedDerogateAttestationValidator implements PackedAttestationValidator {

    private CoseMapper coseMapper = new CoseMapper();

    @Override
    public boolean verify(AuthenticatorAttestation authenticatorAttestation) {
        KeyInfo keyInfo = coseMapper.keyInfo(authenticatorAttestation.getAttestation().getAuthenticatorData().getCredentialPublicKey());

        return this.verifySignature(authenticatorAttestation, keyInfo);
    }

    @Override
    public void verifyX509Certificate(X509Certificate x509Certificate) {
        throw new X5cValidationException("X509 not supported in self attestation");
    }


}

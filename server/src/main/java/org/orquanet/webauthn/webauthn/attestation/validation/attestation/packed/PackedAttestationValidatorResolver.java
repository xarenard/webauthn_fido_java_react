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

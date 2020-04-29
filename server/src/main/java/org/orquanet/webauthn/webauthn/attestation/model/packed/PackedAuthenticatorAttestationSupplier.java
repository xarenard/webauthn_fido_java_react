package org.orquanet.webauthn.webauthn.attestation.model.packed;

import lombok.Builder;
import org.orquanet.webauthn.webauthn.attestation.model.Attestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;

import java.util.function.Supplier;

@Builder
public class PackedAuthenticatorAttestationSupplier implements Supplier<AuthenticatorAttestation> {

    private Attestation attestation;
    private String clientDataJson;

    @Override
    public AuthenticatorAttestation get() {
        if(attestation.getX5c().isPresent()){
            return PackedAuthenticatorFullAttestation.builder().attestation(attestation).clientDataJSON(clientDataJson).build();
        }
        else {
            return PackedAuthenticatorSelfAttestation.builder().attestation(attestation).clientDataJSON(clientDataJson).build();
        }
    }
}

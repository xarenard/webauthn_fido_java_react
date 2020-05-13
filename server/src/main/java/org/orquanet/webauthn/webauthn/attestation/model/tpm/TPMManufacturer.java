package org.orquanet.webauthn.webauthn.attestation.model.tpm;

import lombok.Builder;
import lombok.Getter;

import javax.annotation.processing.Generated;

@Getter
@Builder
public class TPMManufacturer {
    private String id;
    private String name;
    private String namedId;
}

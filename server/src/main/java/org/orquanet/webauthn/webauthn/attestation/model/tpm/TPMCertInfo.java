package org.orquanet.webauthn.webauthn.attestation.model.tpm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TPMCertInfo {
    String magic;
    String type;
    String qualifierSigner;
    byte[] extraData;
    String clockInfo;
    String firmWareVersion;
    String attestedName;
    String attestedQualifiedName;
    byte[] rawCertInfo;

}

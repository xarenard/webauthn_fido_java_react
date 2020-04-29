package org.orquanet.webauthn.webauthn.attestation.model.packed;

import lombok.experimental.SuperBuilder;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

@SuperBuilder
public class PackedAuthenticatorFullAttestation extends AuthenticatorAttestation {

    @Override
    public KeyInfo getKeyInfo() {
        KeyInfo keyInfo = null;
        try {
            Optional<List<byte[]>> certList = this.getAttestation().getX5c();
            byte[] cert = certList.get().get(0);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(cert));

            keyInfo = KeyInfo.builder().publicKey(certificate.getPublicKey()).build();
        } catch(CertificateException e){
            e.printStackTrace();
        }
        return keyInfo;
    }
}

package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;

import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import java.util.Base64;

public interface PackedAttestationValidator {

    public void verifyX509Certificate(X509Certificate x509Certificate);
    public default boolean verifySignature(AuthenticatorAttestation authenticatorAttestation, KeyInfo keyInfo) {
        boolean validSignature = false;
        try {
            byte[] clientDataRaw = Base64.getUrlDecoder().decode(authenticatorAttestation.getClientDataJSON());
            byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);
            ByteArrayOutputStream verificationDataStream = new ByteArrayOutputStream();
            verificationDataStream.write(authenticatorAttestation.getAttestation().getAuthenticatorData().getAuthData());
            verificationDataStream.write(clientDataHash);
            byte[] verificationData = verificationDataStream.toByteArray();
            verificationDataStream.close();

            SignatureVerifier signatureVerifierObject = new SignatureVerifier();
            validSignature = signatureVerifierObject.validate(verificationData, Base64.getDecoder().decode(authenticatorAttestation.getAttestation().getSignature().getBytes()), keyInfo);

        }catch(Exception e){
            e.printStackTrace();
        }
        return validSignature;
    }
    public boolean verify(AuthenticatorAttestation authenticatorAttestation);
}

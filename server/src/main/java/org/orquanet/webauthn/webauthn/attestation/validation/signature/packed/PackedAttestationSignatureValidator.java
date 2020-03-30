package org.orquanet.webauthn.webauthn.attestation.validation.signature.packed;

import org.apache.commons.codec.digest.DigestUtils;
import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.cose.mapper.CoseMapper;
import org.orquanet.webauthn.crypto.signature.SignatureVerifier;
import org.orquanet.webauthn.webauthn.attestation.model.Attestation;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.AttestationValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Function;

public class PackedAttestationSignatureValidator implements Function<AuthenticatorAttestation,Boolean> {

    @Autowired
    @Qualifier("base64urldecoder")
    private Base64.Decoder base64Decoder;

    public static Logger LOGGER = LoggerFactory.getLogger(PackedAttestationSignatureValidator.class);

    @Override
    public Boolean apply(AuthenticatorAttestation authenticatorAttestation) {
        return this.verify(authenticatorAttestation);
    }

    public boolean verify(AuthenticatorAttestation authenticatorAttestation)  {
        CoseMapper cose = new CoseMapper();

        boolean validSignature;
        Attestation attestation = authenticatorAttestation.getAttestation();
        String clientDataJSON = authenticatorAttestation.getClientDataJSON();
        KeyInfo keyinfo = cose.keyInfo(attestation.getAuthenticatorData().getCredentialPublicKey());

        try {
            byte[] clientDataRaw = Base64.getUrlDecoder().decode(clientDataJSON);
            byte[] clientDataHash = DigestUtils.sha256(clientDataRaw);
//            byte[] clientDataHash = DigestUtils.hash(base64Decoder.decode(clientDataJSON.getBytes()), HashAlgorithm.SHA256);
            ByteArrayOutputStream verificationDataStream = new ByteArrayOutputStream();
            verificationDataStream.write(attestation.getAuthenticatorData().getAuthData());
            verificationDataStream.write(clientDataHash);

            byte[] verificationData = verificationDataStream.toByteArray();
            verificationDataStream.close();
            SignatureVerifier signatureVerifierObject = new SignatureVerifier();
            validSignature = signatureVerifierObject.validate(verificationData, Base64.getDecoder().decode(attestation.getSignature().getBytes()), keyinfo);
        }catch( IOException e){
            LOGGER.error(e.getMessage());
            throw new AttestationValidationException(e);
        }
        catch(Exception e){
            LOGGER.error(e.getMessage());
            throw new AttestationValidationException(e);

        }
        return validSignature;
    }

}

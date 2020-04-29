package org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.full;

import org.orquanet.webauthn.crypto.KeyInfo;
import org.orquanet.webauthn.crypto.utils.X509Utils;
import org.orquanet.webauthn.webauthn.attestation.model.AuthenticatorAttestation;
import org.orquanet.webauthn.webauthn.attestation.validation.attestation.packed.PackedAttestationValidator;
import org.orquanet.webauthn.webauthn.attestation.validation.exception.X5cValidationException;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PackedFullAttestationValidator implements PackedAttestationValidator {

    @Override
    public void verifyX509Certificate(X509Certificate x509Certificate) {

        try {
            int version = x509Certificate.getVersion();

            if (version != 3) {
                throw new X5cValidationException("Invalid Certificate Version");
            }

            Principal subjectDN = x509Certificate.getSubjectDN();
            String dn = subjectDN.getName();

            LdapName ldapName = new LdapName(dn);
            List<Rdn> rdns = ldapName.getRdns();

            Map<String, Rdn> rdnsMap = rdns.stream().collect(Collectors.toMap(Rdn::getType, Function.identity()));

            Rdn organisationRDN = rdnsMap.get("OU");
            if(organisationRDN == null || !"Authenticator Attestation".equals(organisationRDN.getValue().toString())){
                throw new X5cValidationException("Invalid Organisation Unit");
            }

            Rdn cnRdn = rdnsMap.get("CN");
            if(cnRdn == null || cnRdn.getValue() == null || "".equals(cnRdn.getValue().toString().trim() )){
                throw new X5cValidationException("Invalid Common name.");
            }

            int basicConstraints = x509Certificate.getBasicConstraints();
            if(basicConstraints != -1){
                throw new X5cValidationException("Invalid Basic Constraints.");
            }

            Set<String> keys = rdnsMap.keySet();
            keys.forEach(s -> {
                System.out.println(s);
                System.out.println(rdnsMap.get(s));
            });


            byte[] idFidoGenCeAaguid = x509Certificate.getExtensionValue("1.3.6.1.4.1.45724.1.1.4");
            System.out.println(idFidoGenCeAaguid.length);
            // if(!Arrays.equals(idFidoGenCeAaguid,aaguid)){
            //   throw new X5cValidationException("Invalid aaguid");
            //}

        }
        catch(InvalidNameException e){
            throw new X5cValidationException(e);
        }
    }

    @Override
    public boolean verify(AuthenticatorAttestation authenticatorAttestation) {
            Optional<List<byte[]>> certs = authenticatorAttestation.getAttestation().getX5c();
            X509Certificate x509Certificate = X509Utils.x509CertificateFromBytesArray(certs.get().get(0));
            this.verifyX509Certificate(x509Certificate);

            KeyInfo keyInfo = KeyInfo.builder().publicKey(x509Certificate.getPublicKey()).build();
            return this.verifySignature(authenticatorAttestation, keyInfo);
    }
}

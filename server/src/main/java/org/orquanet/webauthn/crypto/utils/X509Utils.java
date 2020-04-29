package org.orquanet.webauthn.crypto.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509Utils {

    public static X509Certificate x509CertificateFromBytesArray(final byte[] x509Bytes){
        X509Certificate x509Certificate = null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            x509Certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(x509Bytes));
        } catch(CertificateException e){

        }
        return x509Certificate;
    }
}

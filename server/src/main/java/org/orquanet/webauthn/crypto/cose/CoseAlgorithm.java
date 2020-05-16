package org.orquanet.webauthn.crypto.cose;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;

// TO BE REFACTORED
public enum CoseAlgorithm {
    RS1(-65535, "RSASSA-PKCS1-v1_5 w/ SHA-1", MessageDigestAlgorithms.SHA_1,"SHA1withRSA","RSA"),
    RS512(-259, "RSASSA-PKCS1-v1_5 w/ SHA-512", MessageDigestAlgorithms.SHA_512,"SHA512withRSA","RSA"),
    RS384(-258, "RSASSA-PKCS1-v1_5 w/ SHA-384", MessageDigestAlgorithms.SHA_384,"SHA384withRSA","RSA"),
    RS256(-257, "RSASSA-PKCS1-v1_5 w/ SHA-256", MessageDigestAlgorithms.SHA_256,"SHA256withRSA","RSA"),
    ES256(-7,"ECDSA w/ SHA-256",MessageDigestAlgorithms.SHA_256,"SHA256withECDSA","ECDSA"),
    ES384(-35,"ECDSA w/ SHA-384",MessageDigestAlgorithms.SHA_256,"SHA384withECDSA","ECDSA"),
    ES512(-36,"ECDSA w/ SHA-512",MessageDigestAlgorithms.SHA_256,"SHA512withECDSA","ECDSA");


    private int value;
    private String description;
    private String messageDigestAlgorithm;
    private String signatureAlgorithm;
    private String keyType;

    CoseAlgorithm(int value, String description, String messageDigestAlgorithm, String signatureAlgorithm, String keyType) {
        this.value = value;
        this.description = description;
        this.messageDigestAlgorithm = messageDigestAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
        this.keyType = keyType;
    }

    public Integer getValue(){
        return this.value;
    }

    public String getMessageDigestAlgorithm(){
        return this.messageDigestAlgorithm;
    }

    public String getSignatureAlgorithm(){
        return this.signatureAlgorithm;
    }

    public String getKeyType() {
        return this.keyType;
    }

}

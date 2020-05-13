package org.orquanet.webauthn.webauthn.attestation.model.tpm;
import lombok.Builder;
import lombok.Getter;
import org.orquanet.webauthn.crypto.cose.CoseAlgorithm;

import java.util.Optional;

@Builder
@Getter
public class TPMPubArea<T extends TPMPubArea.TPMAlg> {
    private byte[] rawPubArea;
    private int algorithm;
    private int type;
    private byte[] authPolicy;
    private T alg;
    byte[] unique;

    @Getter
    public static abstract class TPMAlg {

        private byte[] symetric;
        private byte[] scheme;

        public TPMAlg(byte[] symetric, byte[] scheme){
            this.symetric = symetric;
            this.scheme = scheme;
        }
    }
    @Getter

    public static class TPMAlgRSA extends TPMAlg{
        private byte[] keyBytes;
        private byte[] exponent;

        public TPMAlgRSA(byte[] symetric, byte[] scheme, byte[] keyBytes, byte[] exponent){
            super(symetric,scheme);
            this.keyBytes = keyBytes;
            this.exponent = exponent;
        }
    }

    @Getter
    public static class TPMAlgECC extends TPMAlg {
        private byte[] curveId;
        private byte[] kdf;

        public TPMAlgECC(byte[] symetric, byte[] scheme, byte[] curveId, byte[] kdf) {
            super(symetric, scheme);
            this.curveId = curveId;
            this.kdf = kdf;
        }
    }
}

package org.orquanet.webauthn.controller.fido;

import java.security.SecureRandom;
import java.util.Base64;

public class FidoController {

    private static SecureRandom random = new SecureRandom();


    protected static byte[] challenge() {
        return challenge(30);
    }

    protected static byte[] challenge(int size){

        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    protected static String challengeAsString(){
        return Base64.getEncoder().encodeToString(challenge());
    }
}

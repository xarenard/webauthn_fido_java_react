package org.orquanet.webauthn.controller.fido;

import org.orquanet.webauthn.controller.session.WebauthnSession;
import org.orquanet.webauthn.repository.model.FidoUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;

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

    public void initWebauthnSession(String sessionName,HttpServletRequest request, String challenge, FidoUser fidoUser) {
        WebauthnSession webauthnSession = new WebauthnSession();
        webauthnSession.setChallenge(challenge);
        webauthnSession.setFidoUser(fidoUser);
        HttpSession session = request.getSession(true);
        session.setAttribute(sessionName, webauthnSession);
    }
}

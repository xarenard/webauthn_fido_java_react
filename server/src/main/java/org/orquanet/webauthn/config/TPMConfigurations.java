package org.orquanet.webauthn.config;

import lombok.Getter;
import org.orquanet.webauthn.webauthn.attestation.model.tpm.TPMManufacturer;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "tpm-manufacturers")
public class TPMConfigurations {
    private Map<String,TPMManufacturer> tpmManufacturersMap = new HashMap<>();
}

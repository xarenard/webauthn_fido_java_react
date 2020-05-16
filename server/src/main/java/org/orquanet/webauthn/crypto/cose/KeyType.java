package org.orquanet.webauthn.crypto.cose;


import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum KeyType {

    RESERVED,OPK,ECDSA,RSA;

    private static Map<Integer,KeyType> keyTypeTable;

    static {
        KeyType[] keyTypes = KeyType.values();
        keyTypeTable = Arrays.stream(keyTypes).collect(Collectors.toMap(KeyType::ordinal, Function.identity()));
    }

    public static Optional<KeyType> fromInt(Integer keyTypeValue){
        return Optional.ofNullable(keyTypeTable.get(keyTypeValue));
    }
}

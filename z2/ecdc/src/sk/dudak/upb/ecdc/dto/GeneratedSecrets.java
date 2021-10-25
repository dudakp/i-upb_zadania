package sk.dudak.upb.ecdc.dto;

import javax.crypto.SecretKey;
import java.io.Serializable;


public class GeneratedSecrets implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;


    private final SecretKey key;
    private final byte[] iv;

    public GeneratedSecrets(SecretKey key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    public SecretKey getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }
}

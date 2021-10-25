package sk.dudak.upb.ecdc.service;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface SecureKeyService {

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    IvParameterSpec getIv();

    SecretKey getAesSymetricKey();
}

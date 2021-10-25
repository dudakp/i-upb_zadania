package sk.dudak.upb.ecdc.service.impl;

import sk.dudak.upb.ecdc.service.CryptService;
import sk.dudak.upb.ecdc.service.SecureKeyService;
import sk.dudak.upb.ecdc.utils.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SecureKeyServiceImpl implements SecureKeyService {

    public static final String KEY_ALG = "AES";
    private static final int KEY_SIZE = 16;
    private static final String KEY_IV_PATH = "./keys.key";
    private static final int IV_SIZE = 12;

    private PrivateKey sk;
    private PublicKey pk;
    private SecretKey aesSymetricKey;


    private IvParameterSpec iv;
    private final Cipher cipher;

    public SecureKeyServiceImpl(CryptService cryptService) throws IOException, ClassNotFoundException {
        this.cipher = cryptService.getCipher();
        Map<String, Object> generatedSecrets = loadKeys();
        this.sk = (PrivateKey) generatedSecrets.get("private");
        this.pk = (PublicKey) generatedSecrets.get("public");
        this.iv = new IvParameterSpec(((byte[]) generatedSecrets.get("iv")));
        this.aesSymetricKey = generateSecretAesKey();

    }

    private Map<String, Object> loadKeys() throws IOException, ClassNotFoundException {
        File keyFile = new File(KEY_IV_PATH);
        // ak uz aplikacia nevygenerovala kluc tak si ho vygenerujem
        if (!keyFile.exists()) {
            Map<String, Object> rsaKeys = getRSAKeys();
            rsaKeys.put("iv", generateIv().getIV());
            FileUtils.serializeObjectToFile("./keys.key", rsaKeys);
            return rsaKeys;
        }
        // ak uz existuje tak deserializujem ulozeny kluc
        try (FileInputStream fileInputStream = new FileInputStream(KEY_IV_PATH);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return ((Map<String, Object>) objectInputStream.readObject());
        }
    }

    private Map<String, Object> getRSAKeys() {
        KeyPairGenerator keyPairGenerator = null;
        Map<String, Object> keys = new HashMap<>();
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            keys.put("private", privateKey);
            keys.put("public", publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keys;
    }


    public static IvParameterSpec generateIv() {
        byte[] ivBytes = new byte[IV_SIZE];
        new SecureRandom().nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }

    public static SecretKey generateSecretAesKey() {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance("AES");
            generator.init(128); // The AES key size in number of bits
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generator.generateKey();
    }

    @Override
    public PrivateKey getPrivateKey() {
        return sk;
    }

    @Override
    public PublicKey getPublicKey() {
        return pk;
    }

    @Override
    public IvParameterSpec getIv() {
        return this.iv;
    }

    @Override
    public SecretKey getAesSymetricKey() {
        return this.aesSymetricKey;
    }
}

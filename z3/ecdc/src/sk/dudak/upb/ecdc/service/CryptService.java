package sk.dudak.upb.ecdc.service;

import javafx.scene.control.Label;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface CryptService {

    long encryptFile(File file, SecretKey aesSymetricKey, PublicKey key, IvParameterSpec iv, Label statusLabel) throws Exception;

    long decryptFile(File file, PrivateKey key, IvParameterSpec iv, Label dcStatusLabel) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException;

    Cipher getCipher();
}

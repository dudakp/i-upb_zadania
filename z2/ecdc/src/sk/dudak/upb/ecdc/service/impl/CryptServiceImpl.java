package sk.dudak.upb.ecdc.service.impl;

import javafx.scene.control.Label;
import sk.dudak.upb.ecdc.service.CryptService;
import sk.dudak.upb.ecdc.task.CryptTask;
import sk.dudak.upb.ecdc.utils.FileUtils;
import sk.dudak.upb.ecdc.utils.ThreadingUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CryptServiceImpl implements CryptService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";


    private Cipher cipher;

    public CryptServiceImpl() {
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private byte[] encryptSymetricKey(PublicKey key, SecretKey symetricKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // zasifrujem symetricky kluc pomocou RSA
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(symetricKey.getEncoded());
    }

    private byte[] encryptPlainText(FileInputStream inputStream, SecretKey symetricKey, IvParameterSpec iv) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        // zasifrujem plaintext GCM modom
        cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey originalKey = new SecretKeySpec(symetricKey.getEncoded(), 0, symetricKey.getEncoded().length, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, originalKey, new GCMParameterSpec(128, iv.getIV()));
        int ivLength = iv.getIV().length;
        int symmetricKeyLength = symetricKey.getEncoded().length;
        ByteBuffer additionalAuthenticationData = ByteBuffer.allocate(ivLength + symmetricKeyLength);
        additionalAuthenticationData.put(symetricKey.getEncoded());
        cipher.updateAAD(additionalAuthenticationData);
        byte[] encryptedPlaintext = new byte[8192];
        byte[] buffer = new byte[8192];
        while (inputStream.read(buffer) > 0) {
            encryptedPlaintext = cipher.doFinal(buffer);
        }
        return encryptedPlaintext;
    }

    @Override
    public long encryptFile(File file, SecretKey aesSymetricKey, PublicKey key, IvParameterSpec iv, Label statusLabel) throws Exception {
        AtomicLong timeElapsed = new AtomicLong();
        Instant start = Instant.now();
        System.out.println("Started encryption");
        statusLabel.setText("Started encryption");
        String ecFilename = FileUtils.buildEcFilename(file);
        FileInputStream inputStream = new FileInputStream(file);
        try {
            if (cipher == null) {
                throw new Exception("failed to initialize Cipher!");
            }

            byte[] encodedSymetricKey = this.encryptSymetricKey(key, aesSymetricKey, iv.getIV());
            byte[] finalEncryptedPlainText = this.encryptPlainText(inputStream, aesSymetricKey, iv);
            System.out.println(Base64.getEncoder().encodeToString(encodedSymetricKey));
            FileOutputStream outputStream = new FileOutputStream(ecFilename);

            CryptTask task = new CryptTask(() -> {
                try {
                    FileUtils.processFile(inputStream, outputStream, encodedSymetricKey, finalEncryptedPlainText);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
            ThreadingUtils.runTaskInThread(task,
                    workerStateEvent -> {
                        Instant finish = Instant.now();
                        timeElapsed.set(Duration.between(start, finish).toMillis());
                        System.out.println("File encrypted! Saved as: " + ecFilename + ". Duration: " + timeElapsed + " ms");
                        statusLabel.setText("Encryption ended. Took " + timeElapsed + " ms");
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                    workerStateEvent -> {
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );

        } catch (InvalidKeyException
                | IllegalBlockSizeException
                | IOException
                | BadPaddingException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return timeElapsed.longValue();
    }

    private byte[] decryptKeyFromHead(byte[] symetricKey, byte[] iv, PrivateKey publicKey) throws Exception {
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(symetricKey);
    }

    private byte[] decryptCipherText(byte[] cipherText, SecretKey symetricKey, IvParameterSpec iv) throws Exception {
        cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, symetricKey, new GCMParameterSpec(128, iv.getIV()));

        int ivLength = iv.getIV().length;
        int symmetricKeyLength = symetricKey.getEncoded().length;
        ByteBuffer additionalAuthenticationData = ByteBuffer.allocate(ivLength + symmetricKeyLength);
        additionalAuthenticationData.put(symetricKey.getEncoded());
        cipher.updateAAD(additionalAuthenticationData);
        return cipher.doFinal(cipherText);
    }

    @Override
    public long decryptFile(File file, PrivateKey key, IvParameterSpec iv, Label dcStatusLabel) throws IOException {

        AtomicLong timeElapsed = new AtomicLong();
        Instant start = Instant.now();
        System.out.println("Started decryption");
        dcStatusLabel.setText("Started decryption");
        String encryptedFilePostfix = FileUtils.getFilePostfix(file);
        String decryptedFileName = "decrypted." + encryptedFilePostfix;
        File decryptedFile = new File(decryptedFileName);
        if (decryptedFile.exists()) {
            decryptedFileName = "decrypted" + new Random().nextInt(10) + "." + encryptedFilePostfix;
            decryptedFile = new File(decryptedFileName);
        }
        if (!decryptedFile.createNewFile()) {
            System.out.println("Error creating decrypted file!");
            throw new IOException();
        }
        FileOutputStream outputStream = new FileOutputStream(decryptedFile);
        try {

            FileInputStream inputStream = new FileInputStream(file.getPath());
            byte[] decryptedText = new byte[8192];
            try {
                byte[] symetricKey = new byte[256];
                inputStream.read(symetricKey);
                System.out.println(Base64.getEncoder().encodeToString(symetricKey));
                byte[] decryptedSymetricKey = this.decryptKeyFromHead(symetricKey, iv.getIV(), key);
                SecretKeySpec keySpec = new SecretKeySpec(decryptedSymetricKey, "AES");

                int filesize = (int) file.length() - 256;
                int written = 0;
                final int blockSize = 256 * 256;
                while (written < filesize) {
                    byte[] input;
                    if (filesize - written < blockSize) {
                        input = new byte[(filesize - written)];
                    } else {
                        input = new byte[blockSize];
                    }
                    inputStream.read(input);
                    outputStream.write(decryptCipherText(input, keySpec, iv));
                    written += input.length;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] finalDecryptedText = decryptedText;
            CryptTask task = new CryptTask(() -> {
                try {
                    FileUtils.processFile(inputStream, outputStream, finalDecryptedText);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
            String finalDecryptedFileName = decryptedFileName;
            ThreadingUtils.runTaskInThread(task,
                    workerStateEvent -> {
                        Instant finish = Instant.now();
                        timeElapsed.set(Duration.between(start, finish).toMillis());
                        System.out.println("File decrypted! Saved as: " + finalDecryptedFileName + ". Duration: " + timeElapsed + " ms");
                        dcStatusLabel.setText("Decryption ended. Took " + timeElapsed + " ms");
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                    workerStateEvent -> {
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
            decryptedFile.delete();
        }
        return timeElapsed.longValue();
    }

    @Override
    public Cipher getCipher() {
        return this.cipher;
    }

}

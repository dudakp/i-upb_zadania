package sk.dudak.upb.ecdc.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public final class FileUtils {
    public static void serializeObjectToFile(String path, Object object) throws IOException {
        File keyFile = new File(path);
        if (!keyFile.createNewFile()) {
            keyFile.delete();
//            throw new RuntimeException("unable to create key file!");
        }
        try (FileOutputStream fileStream = new FileOutputStream(keyFile);
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
            objectStream.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void processFile(FileInputStream inputStream, FileOutputStream outputStream, byte[]... metadata) throws IOException, IllegalBlockSizeException, BadPaddingException {
        if (metadata.length > 0) {
            for (byte[] m : metadata) {
                outputStream.write(m);
            }
        }
        inputStream.close();
        outputStream.close();
    }


    public static String buildEcFilename(File file) {
        String ogFileName = file.getName();
        String[] splittedByDot = ogFileName.split("\\.");
        String name = splittedByDot[0];
        String postfix = splittedByDot.length > 1 ? splittedByDot[1] : "";
        return name + "_ec" + "." + postfix;
    }

    public static String getFilePostfix(File file) {
        String ogFileName = file.getName();
        String[] splittedByDot = ogFileName.split("\\.");
        return splittedByDot.length > 1 ? splittedByDot[1] : "";
    }
}

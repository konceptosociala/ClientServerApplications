package org.konceptosociala.netpkt.packet;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
    private static final byte[] KEY = "1234567890abcdef".getBytes();

    public static byte[] encrypt(byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"));
        return cipher.doFinal(input);
    }

    public static byte[] decrypt(byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"));
        return cipher.doFinal(input);
    }
}
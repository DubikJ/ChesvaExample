package ua.com.expertsolution.chesva.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncyption {

    private final static String TOKEN_KEY = "fqJfdzGDvfwbedsKSUGty3VZ9taXxMVw";
    private static final String CRYPTER = "AES/ECB/NoPadding";
    private static final String ALHORITM = "AES";
    private static final String CHARSET = "UTF-8";

    public static String encrypt(String textToEncrypt) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(TOKEN_KEY.getBytes(CHARSET), ALHORITM);
        Cipher cipher = Cipher.getInstance(CRYPTER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(textToEncrypt.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String textToDecrypt) throws Exception {

        byte[] encryted_bytes = Base64.decode(textToDecrypt, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(TOKEN_KEY.getBytes(CHARSET), ALHORITM);
        Cipher cipher = Cipher.getInstance(CRYPTER);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encryted_bytes);
        return new String(decrypted, CHARSET);
    }

}

package ua.com.expertsolution.chesva.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import androidx.annotation.Nullable;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by azret.magometov on 08-Nov-16.
 */
@TargetApi(Build.VERSION_CODES.M)
public final class CryptoUtils {
    private static final String TAG = CryptoUtils.class.getSimpleName();

    private static final String KEY_ALIAS = "key_for_pin";
    private static final String KEY_STORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static KeyStore sKeyStore;
    private static KeyPairGenerator sKeyPairGenerator;
    private static Cipher sCipher;

    public static String encode(String inputString) {
        try {
            if (prepare() && initCipher(Cipher.ENCRYPT_MODE)) {
                byte[] bytes = sCipher.doFinal(inputString.getBytes());
                return Base64.encodeToString(bytes, Base64.NO_WRAP);
            }
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    public static String decode(String encodedString, Cipher cipher) {
        try {
            byte[] bytes = Base64.decode(encodedString, Base64.NO_WRAP);
            return new String(cipher.doFinal(bytes));
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static boolean prepare() {
        return getKeyStore() && getCipher() && getKey();
    }


    private static boolean getKeyStore() {
        try {
            sKeyStore = KeyStore.getInstance(KEY_STORE);
            sKeyStore.load(null);
            return true;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private static boolean getKeyPairGenerator() {
        try {
            sKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE);
            return true;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return false;
    }


    @SuppressLint("GetInstance")
    private static boolean getCipher() {
        try {
            sCipher = Cipher.getInstance(TRANSFORMATION);
            return true;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean getKey() {
        try {
            return sKeyStore.containsAlias(KEY_ALIAS) || generateNewKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;

    }


    @TargetApi(Build.VERSION_CODES.M)
    private static boolean generateNewKey() {

        if (getKeyPairGenerator()) {

            try {
                sKeyPairGenerator.initialize(
                        new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                                .setUserAuthenticationRequired(true)
                                .build());
                sKeyPairGenerator.generateKeyPair();
                return true;
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private static boolean initCipher(int mode) {
        try {
            sKeyStore.load(null);

            switch (mode) {
                case Cipher.ENCRYPT_MODE:
                    initEncodeCipher(mode);
                    break;

                case Cipher.DECRYPT_MODE:
                    initDecodeCipher(mode);
                    break;
                default:
                    return false; //this cipher is only for encode\decode
            }
            return true;

        } catch (KeyPermanentlyInvalidatedException exception) {
            deleteInvalidKey();

        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException |
                NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void initDecodeCipher(int mode) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException {
        PrivateKey key = (PrivateKey) sKeyStore.getKey(KEY_ALIAS, null);
        sCipher.init(mode, key);
    }

    private static void initEncodeCipher(int mode) throws KeyStoreException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        PublicKey key = sKeyStore.getCertificate(KEY_ALIAS).getPublicKey();

        // workaround for using public key
        // from https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html
        PublicKey unrestricted = KeyFactory.getInstance(key.getAlgorithm()).generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        // from https://code.google.com/p/android/issues/detail?id=197719
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

        sCipher.init(mode, unrestricted, spec);
    }

    public static void deleteInvalidKey() {
        if (getKeyStore()) {
            try {
                sKeyStore.deleteEntry(KEY_ALIAS);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static FingerprintManagerCompat.CryptoObject getCryptoObject() {
        if (prepare() && initCipher(Cipher.DECRYPT_MODE)) {
            return new FingerprintManagerCompat.CryptoObject(sCipher);
        }
        return null;
    }

    public static String toHexString(byte[] array) {

        String bufferString = "";

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String hexChar = Integer.toHexString(array[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString;
    }

    public static byte[] stringToHexBytes(String rawdata) {

        if (rawdata == null || rawdata.isEmpty()) {
            return null;
        }

        String command = rawdata.replace(" ", "").replace("\n", "");

        if (command.isEmpty() || command.length() % 2 != 0
                || isHexNumber(command) == false) {
            return null;
        }

        return hexString2Bytes(command);
    }

    public static boolean isHexNumber(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        boolean flag = true;

        for (int i = 0; i < string.length(); i++) {
            char cc = string.charAt(i);
            if (!isHexNumber((byte) cc)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private static boolean isHexNumber(byte value) {
        if (!(value >= '0' && value <= '9') && !(value >= 'A' && value <= 'F')
                && !(value >= 'a' && value <= 'f')) {
            return false;
        }
        return true;
    }

    public static byte[] hexString2Bytes(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        int len = string.length();

        if (len == 0)
            return new byte[0];
        if (len % 2 == 1)
            throw new IllegalArgumentException(
                    "string length should be an even number");

        byte[] ret = new byte[len / 2];
        byte[] tmp = string.getBytes();

        for (int i = 0; i < len; i += 2) {
            if (!isHexNumber(tmp[i]) || !isHexNumber(tmp[i + 1])) {
                throw new NumberFormatException(
                        "string contained invalid value");
            }
            ret[i / 2] = uniteBytes(tmp[i], tmp[i + 1]);
        }
        return ret;
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public static String getServioTagCode(byte[] code){
        int lenghtCode = code.length - 2;
        byte[] apdu1 = new byte[lenghtCode];
        for (int i = 0; i < lenghtCode; i++) {
            apdu1[i] = code[lenghtCode - 1 - i];
        }
        return String.valueOf(Long.parseLong(CryptoUtils.toHexString(apdu1).replaceAll(" ", ""), 16));
    }

}
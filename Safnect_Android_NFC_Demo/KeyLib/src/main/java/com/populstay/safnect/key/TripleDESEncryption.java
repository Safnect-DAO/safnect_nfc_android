package com.populstay.safnect.key;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.security.Security;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESEncryption {

    private static final String ALGORITHM = "DESede";
    private static final String ALGORITHM_MODE = "DESede/CBC/PKCS5Padding";
    private static final String PROVIDER = "BC";
    private static final String SALT = "salt1238"; // 用于派生密钥，应保持安全
    private static final int ITERATION_COUNT = 1000; // 派生密钥的迭代次数
    private static final int KEY_SIZE = 192; // 3DES密钥大小

    private static final String PASSWORD = "password";


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static SecretKey generateKey(byte[] salt, String password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", PROVIDER);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] bytes = tmp.getEncoded();
        SecretKey secret = new SecretKeySpec(bytes, 0, 24, ALGORITHM);
        return secret;
    }

    public static String encrypt(String plainText, String password) throws Exception {
        byte[] salt = SALT.getBytes();
        SecretKey key = generateKey(salt, password);
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE, PROVIDER);
        IvParameterSpec iv = new IvParameterSpec(salt);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.toBase64String(encrypted);
    }

    public static String decrypt(String cipherText, String password) throws Exception {
        byte[] salt = SALT.getBytes();
        SecretKey key = generateKey(salt, password);
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE, PROVIDER);
        IvParameterSpec iv = new IvParameterSpec(salt);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decoded = Base64.decode(cipherText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}

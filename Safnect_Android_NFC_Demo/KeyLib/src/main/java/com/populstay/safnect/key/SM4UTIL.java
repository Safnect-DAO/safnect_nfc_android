package com.populstay.safnect.key;


import android.text.TextUtils;
import android.util.Log;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * SM4加密算法 对称加密
 */
public class SM4UTIL {

    /**
     * 算法名称
     */
    private static final String ALGORITHM_NAME = "SM4";

    /**
     * 密钥长度
     */
    private static final int KEY_LENGTH = 32;

    /**
     * 补位字符
     */
    private static final String RESERVE_CHAR = "a";

    /**
     * 加密算法/分组加密模式/分组填充方式
     * PKCS5Padding-以8个字节为一组进行分组加密
     * 定义分组加密模式使用：PKCS7Padding
     * NoPadding
     */
    //private static final String ALGORITHM_NAME_ECB_PADDING = "SM4/CBC/PKCS7Padding";
    private static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/NoPadding";


    private static final String BC = "BC";

    static {
        if (Security.getProvider(BC) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @param key       密钥
     * @return
     * @throws Exception
     */
    public static String encode(String plainText, String key) throws Exception {
        byte[] srcData = plainText.getBytes();
        byte[] keyData = ByteUtils.fromHexString(key);
        return Base64.toBase64String(encrypt(srcData, keyData));
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param key        密钥
     * @return
     * @throws Exception
     */
    public static String decode(String cipherText, String key) throws Exception {
        byte[] cipherData = Base64.decode(cipherText);
        byte[] keyData = ByteUtils.fromHexString(key);
        byte[] ret = decrypt(cipherData, keyData);
        return new String(ret).trim();
    }

    /**
     * 使用字符串作为密钥加密
     *
     * @param plainText 明文
     * @param str       字符串密钥
     * @return
     */
    public static String encodeByStr(String plainText, String str) throws Exception {
        try {
            if (TextUtils.isEmpty(plainText) || TextUtils.isEmpty(str)) {
                throw new Exception("明文或字符串密钥为空");
            }
            // 使用字符串作为密钥进行SM4加密
            return encode(plainText, generateKey(str));
        } catch (Exception e) {
            throw new Exception("加密失败，" + e.getMessage());
        }
    }

    /**
     * 使用字符串作为密钥加密解密
     *
     * @param cipherText 密文
     * @param str        字符串密钥
     * @return
     */
    public static String decodeByStr(String cipherText, String str) throws Exception {
        try {
            if (TextUtils.isEmpty(cipherText) || TextUtils.isEmpty(str)) {
                throw new Exception("密文或字符串密钥为空");
            }
            // 使用字符串作为密钥进行SM4解密
            return decode(cipherText, generateKey(str));
        } catch (Exception e) {
            throw new Exception("解密失败，" + e.getMessage());
        }
    }

    /**
     * 使用字符串作为密钥加密
     *
     * @param plainText 明文
     * @param str       字符串密钥
     * @return
     */
    public static String encodeToHexByStr(String plainText, String str) throws Exception {
        try {
            if (TextUtils.isEmpty(plainText) || TextUtils.isEmpty(str)) {
                throw new Exception("明文或字符串密钥为空");
            }
            // 使用字符串作为密钥进行SM4加密
            return encodeToHex(plainText, generateKey(str));
        } catch (Exception e) {
            throw new Exception("加密失败，" + e.getMessage());
        }
    }

    /**
     * 使用字符串作为密钥加密解密
     *
     * @param cipherText 密文
     * @param str        字符串密钥
     * @return
     */
    public static String decodeFromHexByStr(String cipherText, String str) throws Exception {
        try {
            if (TextUtils.isEmpty(cipherText) || TextUtils.isEmpty(str)) {
                throw new Exception("密文或字符串密钥为空");
            }
            // 使用字符串作为密钥进行SM4解密
            return decodeFromHex(cipherText, generateKey(str));
        } catch (Exception e) {
            throw new Exception("解密失败，" + e.getMessage());
        }
    }



    /**
     * 加密为16进制字符串
     *
     * @param plainText 明文
     * @param key       密钥
     * @return
     * @throws Exception
     */
    public static String encodeToHex(String plainText, String key) throws Exception {
        byte[] srcData = plainText.getBytes();
        byte[] keyData = ByteUtils.fromHexString(key);
        return ByteUtils.toHexString(encrypt(srcData, keyData));
    }

    /**
     * 解密16进制字符串
     *
     * @param cipherText 密文
     * @param key        密钥
     * @return
     * @throws Exception
     */
    public static String decodeFromHex(String cipherText, String key) throws Exception {
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        byte[] keyData = ByteUtils.fromHexString(key);
        byte[] ret = decrypt(cipherData, keyData);
        return new String(ret).trim();
    }

    /**
     * 加密为16进制字符串
     *
     * @param plainText 明文
     * @param key       密钥
     * @return
     * @throws Exception
     */
    public static String encodeHex(String plainText, String key) throws Exception {
        //byte[] srcData = ByteUtils.fromHexString(plainText);
        byte[] srcData = plainText.getBytes();
        byte[] keyData = ByteUtils.fromHexString(key);
        return ByteUtils.toHexString(encrypt(srcData, keyData));
    }

    /**
     * 解密16进制字符串
     *
     * @param cipherText 密文
     * @param key        密钥
     * @return
     * @throws Exception
     */
    public static String decodeHex(String cipherText, String key) throws Exception {
        byte[] cipherData = cipherText.getBytes();
        //byte[] cipherData = ByteUtils.fromHexString(cipherText);
        byte[] keyData = ByteUtils.fromHexString(key);
        byte[] ret = decrypt(cipherData, keyData);
        return new String(ret).trim();
    }

    /**
     * 生成密钥
     * 128 - 32位16进制，256 - 64位16进制
     *
     * @return
     */
    public static String generateKey(String str) {
        // 转为16进制字符串
        String key = ByteUtils.toHexString(str.getBytes(StandardCharsets.UTF_8));
        if (key.length() < KEY_LENGTH) {
            StringBuilder stringBuilder = new StringBuilder(key);
            // 密钥长度不够时补全
            while (stringBuilder.length() < KEY_LENGTH) {
                stringBuilder.append(RESERVE_CHAR);
            }
            return stringBuilder.toString();
        } else {
            // 密钥长度超出时只截取32位
            return key.substring(0, KEY_LENGTH);
        }
    }

    public static String encodeByte(byte[] plainText, String key) throws Exception {
        byte[] keyData = ByteUtils.fromHexString(key);
        return ByteUtils.toHexString(encrypt(plainText, keyData));
    }

    public static String decodeByte(byte[] plainText, String key) throws Exception {
        byte[] keyData = ByteUtils.fromHexString(key);
        return new String(decrypt(plainText, keyData));
    }

    public static byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(ciphertext);
    }


    /**
     * 加密
     *
     * @param plainText 明文
     * @param key       密钥
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] plainText, byte[] key) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainText);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param key        密钥
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] cipherText, byte[] key) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    /**
     * 生成ECB暗号
     *
     * @param algName 算法名称
     * @param mode    模式
     * @param key     密钥
     * @return
     * @throws Exception
     */
    private static Cipher generateEcbCipher(String algName, int mode, byte[] key) throws Exception {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(algName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    private static Cipher generateCbcCipher(String algName, int mode, byte[] key) throws Exception {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(algName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        // iv 与 key 相同，可以设置不同的
        IvParameterSpec ivSpec = new IvParameterSpec(key);
        cipher.init(mode, sm4Key,ivSpec);
        return cipher;
    }

    // 辅助api
    public static String getKey(){
        // 创建一个随机数生成器
        SecureRandom random = new SecureRandom();

        // 创建一个128位的密钥字节数组
        byte[] keyBytes = new byte[16];
        random.nextBytes(keyBytes);

        // 将密钥字节数组转换为十六进制字符串
        String keyString = toHexString(keyBytes);

        Log.d("SM4Util","keyString Data: " + keyString);
        return keyString;
    }

    // 辅助方法：将字节数组转换为十六进制字符串
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 字节数组异或
    public static byte[] xorBytes(byte[] bytes1, byte[] bytes2) {
        byte[] result = new byte[bytes1.length];
        for (int i = 0; i < bytes1.length; i++) {
            result[i] = (byte) (bytes1[i] ^ bytes2[i]);
        }
        return result;
    }
}

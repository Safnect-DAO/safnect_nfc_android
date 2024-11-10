package com.populstay.safnect.key;


import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

public class SM4MacGenerator {

    public static void main(String[] args) throws Exception {
        // 示例密钥（16字节长）
        String key = "0123456789abcdef0123456789abcdef";
        // 示例待签名数据
        String data = "需要签名的数据";

        // 生成SM4 MAC地址
        String macAddress = generateMacAddress(key, data);
        System.out.println("SM4 MAC地址: " + macAddress);
    }

    public static String generateMacAddress(String key, String data) throws Exception {
        // 将字符串转换为字节数组
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        // 创建SM4密钥规范实例
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
        // 初始化SM4 Cipher为加密模式
        Cipher cipher = Cipher.getInstance("SM4");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 分组数据并进行加密
        byte[] encryptedData = cipher.doFinal(dataBytes);

        // 取加密数据的前8字节作为MAC地址
        String macAddress = bytesToHexString(encryptedData, 8);
        return macAddress;
    }

    public static String bytesToHexString(byte[] bytes, int length) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static final String SM4_ALGORITHM = "SM4";

    public static byte[] calculateMAC(byte[] key, byte[] iv, byte[] data) throws Exception {
        // Create a new instance of the SM4 block cipher
        BlockCipher blockCipher = new SM4Engine();
        // Wrap the SM4 block cipher in a CBC mode cipher
        CBCBlockCipher cbcCipher = new CBCBlockCipher(blockCipher);
        // Create a padded buffered block cipher
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcCipher);
        // Set the key and IV parameters
        cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        // Allocate an array for the output
        byte[] output = new byte[cipher.getOutputSize(data.length)];
        // Process the data
        int processed = cipher.processBytes(data, 0, data.length, output, 0);
        // Finish processing the data to generate the MAC
        processed += cipher.doFinal(output, processed);

        // The MAC is the last 16 bytes of the output
        byte[] mac = Arrays.copyOfRange(output, output.length - 16, output.length);
        return mac;
    }

    private static final int BLOCK_SIZE = 16; // SM4 block size in bytes

    public static byte[] calculateSM4CBCMAC(byte[] key, byte[] iv, byte[] message) {
        // Check if the message length is a multiple of BLOCK_SIZE
        if (message.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Message length must be a multiple of " + BLOCK_SIZE);
        }

        // Initialize the SM4 engine and CBC mode
        SM4Engine engine = new SM4Engine();
        BufferedBlockCipher cipher = new BufferedBlockCipher(engine);

        // Set up the parameters
        KeyParameter keyParam = new KeyParameter(key);
        CipherParameters params = new ParametersWithIV(keyParam, iv);

        // Initialize the cipher in encryption mode
        cipher.init(true, params);

        // Allocate an array for the output
        byte[] output = new byte[message.length];

        // Process the data in blocks
        for (int i = 0; i < message.length; i += BLOCK_SIZE) {
            cipher.getUnderlyingCipher().processBlock(message, i, output, i);
        }

        // Finish processing the data to generate the MAC
        try {
            cipher.doFinal(output, message.length);
        } catch (Exception e) {
            throw new RuntimeException("Error during final processing", e);
        }

        // The MAC is the last block of the output
        return Arrays.copyOfRange(output, message.length - BLOCK_SIZE, message.length);
    }

}

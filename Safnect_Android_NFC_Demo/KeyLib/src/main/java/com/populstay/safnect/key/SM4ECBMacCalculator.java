package com.populstay.safnect.key;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;

public class SM4ECBMacCalculator {

    /**
     * 使用SM4算法的ECB模式计算MAC。
     *
     * @param input         输入数据（十六进制字符串）
     * @param key           密钥（十六进制字符串）
     * @param offset        截取起始位置（从0开始计数）
     * @param length        截取长度
     * @param paddingNeeded 是否需要补位
     * @return 计算得到的MAC（十六进制字符串）
     */
    public static String calculateMac(String input, String key, int offset, int length, boolean paddingNeeded) {
        byte[] inputData = Hex.decode(input);
        byte[] keyBytes = Hex.decode(key);

        // 如果需要补位，则进行补位操作
        if (paddingNeeded) {
            inputData = padData(inputData);
        }

        // 使用SM4算法的ECB模式进行加密
        SM4Engine engine = new SM4Engine();
        engine.init(true, new KeyParameter(keyBytes));

        byte[] encryptedData = new byte[inputData.length];
        for (int i = 0; i < inputData.length; i += 16) {
            engine.processBlock(inputData, i, encryptedData, i);
        }

        // 根据截取位置和长度截取MAC
        byte[] mac = new byte[length];
        System.arraycopy(encryptedData, offset, mac, 0, length);

        return Hex.toHexString(mac);
    }

    /**
     * 对数据进行补位。
     *
     * @param data 待补位的数据
     * @return 补位后的数据
     */
    private static byte[] padData(byte[] data) {
        byte[] padded = new byte[16 * ((data.length + 15) / 16)];
        System.arraycopy(data, 0, padded, 0, data.length);
        padded[data.length] = (byte) 0x80;
        return padded;
    }

    public static byte[] calculateMac2(byte[] keyBytes, byte[] data) {
        SM4Engine engine = new SM4Engine();
        engine.init(true, new KeyParameter(keyBytes));

        // 补位逻辑
        byte[] paddedData = padData1(data);

        // ECB 模式加密
        byte[] encrypted = new byte[paddedData.length];
        for (int i = 0; i < paddedData.length; i += 16) {
            engine.processBlock(paddedData, i, encrypted, i);
        }

        // 返回 MAC，这里取加密结果的最后 16 字节
        return java.util.Arrays.copyOfRange(encrypted, 0, 4);
    }

    private static byte[] padData1(byte[] data) {
        byte[] padded = new byte[16 * ((data.length + 15) / 16)];
        System.arraycopy(data, 0, padded, 0, data.length);
        padded[data.length] = (byte) 0x80;
        return padded;
    }

    public static byte[] calculateECBMac(byte[] key, byte[] data) throws Exception {
        // 确保密钥长度为16字节
        if (key.length != 16) {
            throw new IllegalArgumentException("Key must be 16 bytes long.");
        }

        // 确保数据长度是16字节的整数倍
        byte[] paddedData = padDataToBlockSize(data);

        // 初始化SM4加密引擎
        BlockCipher sm4 = new SM4Engine();
        sm4.init(true, new KeyParameter(key));

        // 加密数据
        byte[] encryptedData = new byte[paddedData.length];
        int outputLength = sm4.processBlock(paddedData, 0, encryptedData, 0);
        for (int i = 1, pos = outputLength; i < paddedData.length / sm4.getBlockSize(); i++, pos += sm4.getBlockSize()) {
            outputLength += sm4.processBlock(paddedData, i * sm4.getBlockSize(), encryptedData, pos);
        }

        // 取最后一个加密块作为MAC
        //byte[] mac = Arrays.copyOfRange(encryptedData, outputLength - sm4.getBlockSize(), outputLength);
        byte[] mac = Arrays.copyOfRange(encryptedData, 0, 4);

        return mac;
    }

    private static byte[] padDataToBlockSize(byte[] data) {
        int remainder = data.length % 16;
        if (remainder == 0) {
            return data;
        }

        int padLength = 16 - remainder;
        byte[] paddedData = new byte[data.length + padLength];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        // 填充方式可以是PKCS#7或自定义填充
        for (int i = 0; i < padLength; i++) {
            paddedData[data.length + i] = (byte) padLength;
        }

        return paddedData;
    }
}
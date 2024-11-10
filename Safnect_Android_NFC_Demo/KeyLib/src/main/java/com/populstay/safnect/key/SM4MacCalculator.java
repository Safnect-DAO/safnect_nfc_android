package com.populstay.safnect.key;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class SM4MacCalculator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] computeSM4Mac(String input, String key, int offset, int length, boolean padding) {
        byte[] inputBytes = padding ? padData(input.getBytes()) : input.getBytes();
        byte[] keyBytes = hexStringToByteArray(key);

        try {
            Cipher cipher = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] cipherText = cipher.doFinal(inputBytes);

            byte[] mac = new byte[length];
            System.arraycopy(cipherText, offset, mac, 0, length);
            return mac;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SM4 MAC", e);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static byte[] padData(byte[] data) {
        int paddingSize = 16 - (data.length % 16);
        byte[] paddedData = new byte[data.length + paddingSize];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        paddedData[data.length] = (byte) 0x80;
        for (int i = data.length + 1; i < paddedData.length; i++) {
            paddedData[i] = (byte) 0x00;
        }
        return paddedData;
    }
}
package com.populstay.safnect.sm4;

import android.text.TextUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class Sm4MacUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String KEY_HEX = "63714B39F6B950FA30D8474DD4944D85";

    /**
     * 进行十六进制字符串的异或操作
     * @param hex1 第一个十六进制字符串
     * @param hex2 第二个十六进制字符串
     * @return 异或后的十六进制字符串
     */
    public static String xorHexStrings(String hex1, String hex2) {
        byte[] bytes1 = hexToBytes(hex1);
        byte[] bytes2 = hexToBytes(hex2);
        byte[] xorResult = new byte[bytes1.length];
        for (int i = 0; i < bytes1.length; i++) {
            xorResult[i] = (byte) (bytes1[i] ^ bytes2[i]);
        }
        return bytesToHex(xorResult);
    }

    /**
     * 将十六进制字符串转换为字节数组
     * @param hexStr 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    /**
     * 使用SM4算法进行加密
     * @param key 密钥
     * @param data 待加密数据
     * @return 加密后的数据
     */
    public static byte[] sm4Encrypt(byte[] key, byte[] data) {
        try {
            org.bouncycastle.crypto.BlockCipher blockCipher = new org.bouncycastle.crypto.engines.SM4Engine();
            org.bouncycastle.crypto.BufferedBlockCipher cipher = new org.bouncycastle.crypto.BufferedBlockCipher(blockCipher);
            // 使用零向量
            byte[] iv = new byte[cipher.getBlockSize()];
            cipher.init(true, new org.bouncycastle.crypto.params.KeyParameter(key));
            cipher.reset();
            cipher.processBytes(iv, 0, iv.length, iv, 0);
            byte[] output = new byte[cipher.getOutputSize(data.length)];
            cipher.processBytes(data, 0, data.length, output, 0);
            cipher.doFinal(output, output.length - cipher.getOutputSize(0));
            return Arrays.copyOf(output, cipher.getOutputSize(0));
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }

    public static boolean isLengthMultipleOf32(String s) {
        return s.length() % 32 == 0;
    }

    public static List<String> splitStringByLength(String s, int length) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < s.length()) {
            int end = Math.min(start + length, s.length());
            result.add(s.substring(start, end));
            start += length;
        }
        return result;
    }

    public static LinkedList<String> createSubstringsQueue(String s, int length) {
        LinkedList<String> queue = new LinkedList<>();
        int start = 0;
        while (start < s.length()) {
            int end = Math.min(start + length, s.length());
            queue.offer(s.substring(start, end)); // 使用offer添加到队列尾部
            start += length;
        }
        return queue;
    }

    public static String dealData(String iv,String key,String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchProviderException {
        // 第一步异或
        String xorResult1 = xorHexStrings(iv, data);
        System.out.println("异或结果: " + xorResult1);
        // SM4加密
        //byte[] encryptionKey = hexToBytes(key);
        byte[] encrypted1 = Sm4Util.encrypt_ECB_NoPadding(ByteUtils.fromHexString(key),ByteUtils.fromHexString(xorResult1));
        String encryptedHex1 = bytesToHex(encrypted1);
        System.out.println("加密结果: " + encryptedHex1);

        return encryptedHex1;
    }

    // 如果一个字符串的长度不是32的倍数，则使用0补位
    public static String padToNearestThirtyTwo(String s) {
        int targetLength = ((s.length() / 32) + 1) * 32; // 计算最近的32的倍数值
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < targetLength) {
            sb.append('0'); // 使用'0'进行填充
        }
        return sb.toString();
    }

    // 十进制整数转换为偶数位十六进制数
    public static String convertToEvenHex(int num) {
        String hex = Integer.toHexString(num);
        if (hex.length() % 2!= 0) {
            hex = "0" + hex;
        }
        return hex;
    }

        /**
         * 将输入字符串补位至32的倍数长度
         * @param str 输入字符串
         * @return 补位后的字符串
         */
        public static String padToMultipleOf32(String str) {
            int originalLength = str.length();
            if (originalLength == 32){
                return str;
            }

            // 计算最近的32的倍数
            int targetLength = ((originalLength + 31) / 32) * 32;
            int paddingLength = targetLength - originalLength;

            StringBuilder sb = new StringBuilder(str);

            boolean firstPad = true;

            while (paddingLength > 0) {
                if (firstPad) {
                    sb.append("8");
                    paddingLength -= 1;
                    firstPad = false;
                    if (paddingLength == 0){
                        sb.append("00000000000000000000000000000000");
                    }
                } else {
                    sb.append("0");
                    paddingLength -= 1;
                }
            }

            if (sb.length() < 64){
               // sb.append("00000000000000000000000000000000");
            }

            return sb.toString();
        }

        /**
         * 将输入字符串补位至32的倍数长度
         * @param str 输入字符串
         * @return 补位后的字符串
         */
        public static String padToMultipleOf32_2(String str) {
            int originalLength = str.length();
            // 计算最近的32的倍数
            int targetLength = ((originalLength + 31) / 32) * 32;
            int paddingLength = targetLength - originalLength;

            StringBuilder sb = new StringBuilder(str);
            // 使用80和00补位
            while (paddingLength > 0) {
                if (paddingLength >= 2) {
                    sb.append("80");
                    paddingLength -= 2;
                } else {
                    sb.append("00");
                    break;
                }
            }

            while (paddingLength > 0) {
                sb.append("00");
                paddingLength -= 2;
            }

            return sb.toString();
        }


    public static String suffixPattern(String str,String suffix) {
        int index = str.lastIndexOf(suffix);
        if (index != -1) {
            str = str.substring(0, index);
        }

        return str;
    }

    public static String getSm4mac(String iv,String key,String mac_data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        if (TextUtils.isEmpty(mac_data)){
            return null;
        }

        if (!isLengthMultipleOf32(mac_data)){
            // mac_data =  padToNearestThirtyTwo(mac_data);
            //mac_data += "0000000000000000000000";
            //mac_data += "00000000000000000000000000000000";
            System.out.println("getSm4mac补位结果: " + mac_data);
        }



        //List<String> dataPartList = splitStringByLength(mac_data,32);
        Queue<String> queue = createSubstringsQueue(mac_data,32);
        String lastSm4Result = null;
        while (!queue.isEmpty()) {
            String part = queue.poll();
            String curIv = TextUtils.isEmpty(lastSm4Result) ? iv : lastSm4Result;
            lastSm4Result = dealData(curIv,key,part);

        }

        return lastSm4Result;

//        String firstPart = mac_data.substring(0,32);
//        String secondPart = mac_data.substring(32,64);
//
//        // 第一步异或
//        String xorResult1 = xorHexStrings(iv, firstPart);
//        System.out.println("第一次异或结果: " + xorResult1);
//        // SM4加密
//        byte[] encryptionKey = hexToBytes(key);
//        byte[] encrypted1 = SM4Util.encrypt_ECB_NoPadding(ByteUtils.fromHexString(key),ByteUtils.fromHexString(xorResult1));
//        String encryptedHex1 = bytesToHex(encrypted1);
//        System.out.println("第一次加密结果: " + encryptedHex1);
//
//        // 第二步异或
//        String xorResult2 = xorHexStrings(encryptedHex1, secondPart);
//        System.out.println("第二次异或结果: " + xorResult2);
//        // SM4加密
//        byte[] encrypted2 = SM4Util.encrypt_ECB_NoPadding(encryptionKey,ByteUtils.fromHexString(xorResult2));
//        String encryptedHex2 = bytesToHex(encrypted2);
//        System.out.println("最终加密结果: " + encryptedHex2);
//
//        return encryptedHex2;
    }
}

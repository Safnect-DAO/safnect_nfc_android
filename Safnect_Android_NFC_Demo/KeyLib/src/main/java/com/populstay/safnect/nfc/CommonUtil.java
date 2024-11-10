package com.populstay.safnect.nfc;

public class CommonUtil {

    //字节数组转换十六进制
    public static String byteArrayToHexString(byte[] array) {
        int i, j, in;
        String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F" };
        String out = "";
        for (j = 0; j < array.length; ++j) {
            in = (int) array[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    // 去除空格、制表符、换行符和回车符
    public static String removeSpacesAndNewLines(String input) {
        // 使用正则表达式去除所有空白字符
        return input.replaceAll("\\s+", "");
    }

}

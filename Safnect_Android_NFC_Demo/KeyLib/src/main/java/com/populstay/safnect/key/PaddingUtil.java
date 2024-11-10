package com.populstay.safnect.key;
import java.nio.charset.StandardCharsets;

public class PaddingUtil {

    private static final String FIRST_PADDING_BYTE = "80";
    private static final String SUBSEQUENT_PADDING_BYTE = "00";

    public static String padTo16Multiple(String input) {
        int inputLength = input.length();
        int paddingSize = (32 - (inputLength % 32)) % 32;
        if (0 == paddingSize){
            return input;
        }
        if (paddingSize < 2){
            input += ("8" + "000000000000000000");
        }else if (paddingSize == 2){
            input += ("80" + "000000000000000000");
        }else {
            input += "80";
            for (int i = 0; i < paddingSize - 2; i++) {
                input += "0";
            }
        }

        return input;
    }
}
package com.otosone.bssmgr.utils;

import java.math.BigInteger;

public class HexToBinUtil {
    public static String hexToBin(String s) {
        String bin = new BigInteger(s, 16).toString(2);
        int padding = 32 - bin.length();
        for (int i = 0; i < padding; i++) {
            bin = "0" + bin;
        }
        return bin;
    }
}

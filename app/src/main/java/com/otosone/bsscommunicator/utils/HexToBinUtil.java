package com.otosone.bsscommunicator.utils;

import java.math.BigInteger;

public class HexToBinUtil {
    public static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }
}

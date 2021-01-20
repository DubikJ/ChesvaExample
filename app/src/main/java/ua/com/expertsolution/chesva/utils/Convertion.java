package ua.com.expertsolution.chesva.utils;

import java.math.BigInteger;

public class Convertion {

    public static String asciiToHex(String ascii) {
        return String.format("%04x", new BigInteger(1, ascii.getBytes()));
    }

    public static String hexToAscii(String s) {
        int n = s.length();
        StringBuilder sb = new StringBuilder(n / 2);
        for (int i = 0; i < n; i += 2) {
            char a = s.charAt(i);
            char b = s.charAt(i + 1);
            sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
        }
        return sb.toString();
    }

    private static int hexToInt(char ch) {
        if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
        if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
        if ('0' <= ch && ch <= '9') { return ch - '0'; }
        throw new IllegalArgumentException(String.valueOf(ch));
    }
}

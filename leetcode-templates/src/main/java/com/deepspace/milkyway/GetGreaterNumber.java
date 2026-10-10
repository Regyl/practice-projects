package com.deepspace.milkyway;

public class GetGreaterNumber {

    public String doSomething(String a, String b) {
        if (b == null || b.isEmpty() || a == null || a.isEmpty()) {
            return a;
        }

        char max = getGreatest(b);
        char[] aChars = a.toCharArray();
        for (int i = 0; i < aChars.length; i++) {
            if (max > aChars[i]) {
                aChars[i] = max;
                break;
            }
        }

        return new String(aChars);
    }

    private char getGreatest(String s) {
        char[] chars = s.toCharArray();
        char max = chars[0];
        for (int i = 1; i < chars.length; i++) {
            if (max < chars[i]) {
                max = chars[i];
            }
        }
        return max;
    }
}

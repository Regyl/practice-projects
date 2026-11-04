package com.github.regyl.urlshortener.withoutduplicates.service;

import java.security.SecureRandom;
import java.util.Random;

public class PostfixGeneratorService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Random random = new SecureRandom();
    private final int length;

    public PostfixGeneratorService(int length) {
        this.length = length;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int val = random.nextInt(0, ALPHABET.length());
            sb.append(ALPHABET.charAt(val));
        }
        return sb.toString();
    }
}

package com.github.regyl.urlshortener.withduplicates.service;

import com.github.regyl.urlshortener.withduplicates.model.GlobalConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
@RequiredArgsConstructor
public class RandomPostfixGeneratorService {

    private static final String ALLOWED_SYMBOLS = "abcdefghigklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();


    public String generatePostfix() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < GlobalConstants.DEFAULT_LENGTH; i++) {
            int next = RANDOM.nextInt(0, ALLOWED_SYMBOLS.length());
            sb.append(ALLOWED_SYMBOLS.charAt(next));
        }

        return sb.toString();
    }
}

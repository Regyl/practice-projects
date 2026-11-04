package com.github.regyl.urlshortener.withoutduplicates;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.urlshortener.withoutduplicates.service.PostfixGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Given info.
 * 62 symbols (lower + capital + digits).
 * length: 5.
 * max values: 62^5=916 000 000
 * expected value for birth problem: sqrt(916 000 000 * 2)=42 804
 */
@Slf4j
@DefaultUnitTest
class BirthProblemTest {

    private PostfixGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new PostfixGeneratorService(5);
    }

    @Test
    void check() {
        Set<String> hashes = new HashSet<>();
        int iterations = 42_804;
        for (int i = 0; i < iterations; i++) {
            String hash = service.generate();
            while (!hashes.add(hash)) {
                hash = service.generate();
            }
        }

        int duplicateCounter = 0;
        int duplicateIterations = 1_000_000;
        for (int i = 0; i < duplicateIterations; i++) {
            String hash = service.generate();
            if (hashes.contains(hash)) {
                duplicateCounter++;
            }
        }
        log.info("Hash size {}", hashes.size());
        log.info("Duplicate quantity {}", duplicateCounter);
        log.info("Duplicate probability {}%", duplicateCounter / duplicateIterations * 100);
    }
}

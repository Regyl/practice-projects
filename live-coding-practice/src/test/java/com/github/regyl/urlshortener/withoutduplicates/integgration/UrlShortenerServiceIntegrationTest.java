package com.github.regyl.urlshortener.withoutduplicates.integgration;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.urlshortener.withoutduplicates.service.PostfixGeneratorService;
import com.github.regyl.urlshortener.withoutduplicates.service.UrlShortenerService;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@DefaultUnitTest
class UrlShortenerServiceIntegrationTest {

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        PostfixGeneratorService postfixGeneratorService = new PostfixGeneratorService(5);
        service = new UrlShortenerService(postfixGeneratorService, "http://tiny.revolut.com/");
    }

    @Test
    void test_async() {
        int iterations = 1_000;

        List<CompletableFuture<String>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < iterations; i++) {
                String originalUrl = "http://google.com/" + (i % 100);
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> service.shorten(originalUrl), executor);
                futures.add(future);
            }
        }

        Map<String, List<Pair<String, String>>> res = futures.stream().map(item -> {
            try {
                return item.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).filter(Objects::nonNull)
                .map(item -> Pair.of(item, service.getOriginal(item))).collect(Collectors.groupingBy(Pair::getKey));

        Assertions.assertThat(res).allSatisfy((key, value) -> {
            Assertions.assertThat(new HashSet<>(value)).hasSize(1);
        });
    }
}

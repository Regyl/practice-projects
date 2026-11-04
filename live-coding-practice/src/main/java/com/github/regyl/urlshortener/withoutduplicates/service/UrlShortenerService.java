package com.github.regyl.urlshortener.withoutduplicates.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UrlShortenerService {

    private final Map<String, String> cache = new HashMap<>();
    private final Map<String, String> reverseCache = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final PostfixGeneratorService postfixGeneratorService;
    private final String baseUrl;

    public UrlShortenerService(PostfixGeneratorService postfixGeneratorService, String baseUrl) {
        this.postfixGeneratorService = postfixGeneratorService;
        this.baseUrl = baseUrl;
    }

    public String shorten(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException();
        }

        lock.writeLock().lock();
        try {
            return shorten0(originalUrl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String shorten0(String originalUrl) {
        if (reverseCache.containsKey(originalUrl)) {
            return baseUrl + reverseCache.get(originalUrl);
        }

        String postfix = postfixGeneratorService.generate();
        String res = cache.putIfAbsent(postfix, originalUrl);
        while (!originalUrl.equals(res)) {
            postfix = postfixGeneratorService.generate();
            res = cache.putIfAbsent(postfix, originalUrl);
        }

        reverseCache.put(originalUrl, postfix);
        return baseUrl + postfix;
    }

    public String getOriginal(String shortenUrl) {
        if (shortenUrl == null || shortenUrl.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (!shortenUrl.startsWith(baseUrl)) {
            throw new IllegalArgumentException("Wrong base url: " + shortenUrl);
        }

        String postfix =  shortenUrl.substring(baseUrl.length());
        if (postfix == null || postfix.isBlank()) {
            throw new IllegalArgumentException();
        }

        lock.readLock().lock();
        try {
            return cache.get(postfix);
        } finally {
            lock.readLock().unlock();
        }
    }
}

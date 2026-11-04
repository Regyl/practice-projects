package com.github.regyl.urlshortener.withduplicates.service;

import com.github.regyl.urlshortener.withduplicates.exception.InvalidShortenUrl;
import com.github.regyl.urlshortener.withduplicates.exception.InvalidSourceUrl;
import com.github.regyl.urlshortener.withduplicates.model.GlobalConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private static final String BASE_URL = "https://revoluttiny.com/";

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
    private final RandomPostfixGeneratorService randomPostfixGeneratorService;

    @Override
    public String getOriginal(String shortenUrl) {
        String postfix = extractPostfix(shortenUrl);
        return cache.get(postfix);
    }

    private String extractPostfix(String shortenUrl) {
        if (shortenUrl == null || shortenUrl.isBlank()) {
            throw new InvalidShortenUrl("Source url is empty!");
        }

        if (!shortenUrl.startsWith(BASE_URL)) {
            throw new InvalidShortenUrl("Source url should start with " + BASE_URL);
        }

        String postfix = shortenUrl.substring(BASE_URL.length());
        if (postfix.length() != GlobalConstants.DEFAULT_LENGTH) {
            throw new InvalidShortenUrl("Source url should be length of " + GlobalConstants.DEFAULT_LENGTH);
        }
        return postfix;
    }

    @Override
    public String shorten(String originalUrl) {
        try {
            return shorten0(originalUrl);
        } catch (MalformedURLException e) {
            String msg = String.format("Url %s is not valid!", originalUrl);
            throw new InvalidSourceUrl(msg);
        }
    }

    private String shorten0(String originalUrl) throws MalformedURLException {
        //validation source url
        new URL(originalUrl);

        String postfix = randomPostfixGeneratorService.generatePostfix();
        String res = cache.putIfAbsent(postfix, originalUrl);

        while (!originalUrl.equals(res)) {
            postfix = randomPostfixGeneratorService.generatePostfix();
            res = cache.putIfAbsent(postfix, originalUrl);
        }

        return BASE_URL + postfix;
    }

}
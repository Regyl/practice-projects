package com.github.regyl.urlshortener.withduplicates.service;

public interface UrlShortenerService {

    String shorten(String originalUrl);

    String getOriginal(String shortenUrl);
}

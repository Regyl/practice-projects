package com.github.regyl.urlshortener.withduplicates.exception;

import com.github.regyl.exception.NonRetryableException;

public class InvalidShortenUrl extends NonRetryableException {

    public InvalidShortenUrl(String s) {
        super(s);
    }
}

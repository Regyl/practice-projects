package com.github.regyl.urlshortener.withduplicates.exception;

import com.github.regyl.exception.NonRetryableException;

public class InvalidSourceUrl extends NonRetryableException {
    public InvalidSourceUrl(String message) {
        super(message);
    }
}

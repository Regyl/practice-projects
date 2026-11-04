package com.github.regyl.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }
}

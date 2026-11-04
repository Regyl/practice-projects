package com.github.regyl.loadbalancer.exception;

import com.github.regyl.exception.NonRetryableException;

public class DuplicateInstanceException extends NonRetryableException {
    public DuplicateInstanceException(String message) {
        super(message);
    }
}

package com.github.regyl.loadbalancer.exception;

import com.github.regyl.exception.NonRetryableException;

public class TooManyRegisteredInstances extends NonRetryableException {

    public TooManyRegisteredInstances(String message) {
        super(message);
    }
}

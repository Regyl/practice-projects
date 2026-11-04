package com.github.regyl.hotelbooking.exception;

import com.github.regyl.exception.NonRetryableException;

public class OverlappingPeriodException extends NonRetryableException {
    public OverlappingPeriodException(String message) {
        super(message);
    }
}

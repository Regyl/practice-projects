package com.github.regyl.hotelbooking.exception;

import com.github.regyl.exception.NonRetryableException;

public class HotelNotFoundException extends NonRetryableException {
    public HotelNotFoundException(String message) {
        super(message);
    }
}

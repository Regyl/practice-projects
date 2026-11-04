package com.github.regyl.moneytransfer.exception;

import com.github.regyl.exception.NonRetryableException;

public class NotEnoughMoneyException extends NonRetryableException {
    public NotEnoughMoneyException() {
        super();
    }
}

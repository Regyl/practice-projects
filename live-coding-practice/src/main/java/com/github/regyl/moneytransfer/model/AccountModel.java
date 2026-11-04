package com.github.regyl.moneytransfer.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class AccountModel {

    private final Long id;
    private final ReentrantLock lock;
    @Setter
    private BigDecimal amount;

    public AccountModel(Long id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
        this.lock = new ReentrantLock(true); //important
    }
}

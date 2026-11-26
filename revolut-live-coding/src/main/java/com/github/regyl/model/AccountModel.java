package com.github.regyl.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class AccountModel {

    private final String id;
    private BigDecimal amount;
    private final ReentrantLock lock;

    public AccountModel(String id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
        this.lock = new ReentrantLock(true);
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountModel that = (AccountModel) o;
        return Objects.equals(id, that.id) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount);
    }
}

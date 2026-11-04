package com.github.regyl.moneytransfer.service;

import java.math.BigDecimal;

public interface AccountManagerService {

    void transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount);

    /**
     * Increases the amount on account.
     * @return the result amount on account.
     */
    BigDecimal deposit(Long accountId, BigDecimal amount);

    /**
     * Decreases the amount on account.
     * @return the result amount on account.
     */
    BigDecimal withdraw(Long accountId, BigDecimal amount);
}

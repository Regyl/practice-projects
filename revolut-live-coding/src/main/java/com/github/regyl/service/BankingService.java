package com.github.regyl.service;

import com.github.regyl.exception.DuplicateAccountException;
import com.github.regyl.exception.ProhibitedInitialAmountException;
import com.github.regyl.model.AccountModel;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentMap;

public class BankingService {

    private final ConcurrentMap<String, AccountModel> accounts;

    public BankingService(ConcurrentMap<String, AccountModel> accounts) {
        this.accounts = accounts;
    }

    public AccountModel openAccount(String accountNumber, BigDecimal amount) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Wrong account number");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            String msg = String.format("Initial amount is prohibited: [%s]", amount);
            throw new ProhibitedInitialAmountException(msg);
        }

        AccountModel account = new AccountModel(accountNumber, amount);
        AccountModel res = accounts.putIfAbsent(accountNumber, account);
        if (res != null) {
            String msg = String.format("Account with id %s already exists", accountNumber);
            throw new DuplicateAccountException(msg);
        }
        return account;
    }

    public BigDecimal getBalance(String accountNumber) {
        return accounts.get(accountNumber).getAmount();
    }

    public BigDecimal deposit(String id, BigDecimal amount) {
        //checks
        AccountModel account = accounts.get(id);
        account.getLock().lock();
        try {
            return deposit0(account, amount);
        } finally {
            account.getLock().unlock();
        }
    }

    private BigDecimal deposit0(AccountModel account, BigDecimal amount) {
        BigDecimal res = account.getAmount().add(amount);
        account.setAmount(res);
        return res;
    }

    public BigDecimal withdraw(String id, BigDecimal amount) {
        //checks
        AccountModel account = accounts.get(id);
        account.getLock().lock();
        try {
            return withdraw0(account, amount);
        } finally {
            account.getLock().unlock();
        }
    }

    private BigDecimal withdraw0(AccountModel account, BigDecimal amount) {
        BigDecimal res = account.getAmount().subtract(amount);
        if (res.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Not enough money");
        }
        account.setAmount(res);
        return res;
    }
}

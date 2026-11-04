package com.github.regyl.moneytransfer.service;

import com.github.regyl.moneytransfer.exception.NotEnoughMoneyException;
import com.github.regyl.moneytransfer.model.AccountModel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

@RequiredArgsConstructor
public class AccountManagerServiceImpl implements AccountManagerService {

    private static final Comparator<AccountModel> ACCOUNT_ID_COMPARATOR = Comparator.comparingLong(AccountModel::getId);

    private final Map<Long, AccountModel> accounts;

    @Override
    public void transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        AccountModel source = accounts.get(sourceAccountId);
        AccountModel target = accounts.get(targetAccountId);

        AccountModel less = ACCOUNT_ID_COMPARATOR.compare(source, target) < 0 ? source : target;
        AccountModel greater = ACCOUNT_ID_COMPARATOR.compare(source, target) > 0 ? source : target;

        less.getLock().lock();
        try {
            greater.getLock().lock();
            try {
                transfer0(source, target, amount);
            } finally {
                greater.getLock().unlock();
            }
        } finally {
            less.getLock().unlock();
        }
    }

    private void transfer0(AccountModel source, AccountModel target, BigDecimal amount) {
        withdraw0(source, amount);
        deposit0(target, amount);
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal deposit(Long accountId, BigDecimal amount) {
        AccountModel account = accounts.get(accountId);
        account.getLock().lock();
        try {
            return deposit0(account, amount);
        } finally {
            account.getLock().unlock();
        }
    }

    private BigDecimal deposit0(AccountModel account, BigDecimal amount) {
        BigDecimal newAmount = account.getAmount().add(amount);
        account.setAmount(newAmount);
        return newAmount;
    }

    /** {@inheritDoc}*/
    @Override
    public BigDecimal withdraw(Long accountId, BigDecimal amount) {
        AccountModel account = accounts.get(accountId);
        account.getLock().lock();
        try {
            return withdraw0(account, amount);
        } finally {
            account.getLock().unlock();
        }
    }

    private BigDecimal withdraw0(AccountModel account, BigDecimal amount) {
        BigDecimal newAmount = account.getAmount().subtract(amount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException();
        }

        account.setAmount(newAmount);
        return newAmount;
    }
}

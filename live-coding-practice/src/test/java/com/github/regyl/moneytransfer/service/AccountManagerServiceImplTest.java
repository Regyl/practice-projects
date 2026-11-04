package com.github.regyl.moneytransfer.service;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.moneytransfer.exception.NotEnoughMoneyException;
import com.github.regyl.moneytransfer.model.AccountModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@DefaultUnitTest
class AccountManagerServiceImplTest {

    private static final AtomicLong COUNTER = new AtomicLong(1);

    @InjectMocks
    private AccountManagerServiceImpl service;

    @Spy
    private Map<Long, AccountModel> accounts = new HashMap<>();

    @Test
    void deposit_should_increase_amount_on_account() {
        //given
        Long id = COUNTER.getAndIncrement();
        AccountModel account = new AccountModel(id, new BigDecimal("1000"));
        accounts.put(account.getId(), account);

        //when
        BigDecimal actualBalance = service.deposit(id, BigDecimal.TEN);

        //then
        Assertions.assertThat(actualBalance.longValue()).isEqualTo(1010L);
    }

    @Test
    void withdraw_should_decrease_amount_on_account() {
        //given
        Long id = COUNTER.getAndIncrement();
        AccountModel account = new AccountModel(id, new BigDecimal("1000"));
        accounts.put(id, account);

        //when
        BigDecimal actualBalance = service.withdraw(id, BigDecimal.TEN);

        //then
        Assertions.assertThat(actualBalance.longValue()).isEqualTo(990L);
    }

    @Test
    void withdraw_should_throw_exception_on_not_enough_balance() {
        //given
        Long id = COUNTER.getAndIncrement();
        AccountModel account = new AccountModel(id, new BigDecimal("8"));
        accounts.put(id, account);

        //then
        Assertions.assertThatThrownBy(() -> service.withdraw(id, BigDecimal.TEN))
                .isInstanceOf(NotEnoughMoneyException.class);
    }

    @Test
    void transfer_should_decrease_amount_on_source_and_increase_on_target() {
        //given
        Long sourceId = COUNTER.getAndIncrement();
        Long targetId = COUNTER.getAndIncrement();
        AccountModel sourceAccount = new AccountModel(sourceId, new BigDecimal("1000"));
        AccountModel targetAccount = new AccountModel(targetId, new BigDecimal("1000"));
        accounts.put(sourceId, sourceAccount);
        accounts.put(targetId, targetAccount);

        //when
        service.transfer(sourceId, targetId, BigDecimal.TEN);

        //then
        Assertions.assertThat(sourceAccount.getAmount().longValue()).isEqualTo(990L);
        Assertions.assertThat(targetAccount.getAmount().longValue()).isEqualTo(1010L);
    }

    @Test
    void transfer_should_throw_exception_on_not_enough_money_at_source() {
        //given
        Long sourceId = COUNTER.getAndIncrement();
        Long targetId = COUNTER.getAndIncrement();
        AccountModel sourceAccount = new AccountModel(sourceId, BigDecimal.TWO);
        AccountModel targetAccount = new AccountModel(targetId, new BigDecimal("1000"));
        accounts.put(sourceId, sourceAccount);
        accounts.put(targetId, targetAccount);

        //then
        Assertions.assertThatThrownBy(() -> service.transfer(sourceId, targetId, BigDecimal.TEN))
                .isInstanceOf(NotEnoughMoneyException.class);
    }

    @Test
    void validate_async_transfer() {
        Long accountId1 = COUNTER.getAndIncrement();
        Long accountId2 = COUNTER.getAndIncrement();
        Long accountId3 = COUNTER.getAndIncrement();
        AccountModel account1 = new AccountModel(accountId1, new BigDecimal("1000"));
        AccountModel account2 = new AccountModel(accountId2, new BigDecimal("1000"));
        AccountModel account3 = new AccountModel(accountId3, new BigDecimal("1000"));
        accounts.put(accountId1, account1);
        accounts.put(accountId2, account2);
        accounts.put(accountId3, account3);

        int iterations = 100_000;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < iterations; i++) {
                executor.submit(() -> service.transfer(accountId1, accountId2, BigDecimal.ONE));
                executor.submit(() -> service.transfer(accountId2, accountId3, BigDecimal.ONE));
                executor.submit(() -> service.transfer(accountId3, accountId1, BigDecimal.ONE));
            }
        }

        Long overallSum = account1.getAmount().longValue() + account2.getAmount().longValue() + account3.getAmount().longValue();
        Assertions.assertThat(overallSum).isEqualTo(3000L);
    }
}

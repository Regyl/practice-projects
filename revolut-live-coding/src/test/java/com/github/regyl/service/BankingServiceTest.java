package com.github.regyl.service;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.exception.DuplicateAccountException;
import com.github.regyl.exception.ProhibitedInitialAmountException;
import com.github.regyl.model.AccountModel;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DefaultUnitTest
class BankingServiceTest {

    @InjectMocks
    private BankingService service;

    @Spy
    private ConcurrentMap<String, AccountModel> accounts = new ConcurrentHashMap<>();

    @Test
    void openAccount_should_create_account_at_table() {
        //given
        String id = "asdasda";
        BigDecimal amount =  BigDecimal.ONE;

        //when
        AccountModel account = service.openAccount(id, amount);

        //then
        assertThat(account).isNotNull();
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getAmount()).isEqualTo(amount);
    }

    @Test
    void openAccount_should_throw_exception_on_invalid_amount() {
        //given
        String id = "asdasda1";
        BigDecimal amount =  new BigDecimal("-1");

        //then
        assertThatCode(() -> service.openAccount(id, amount)).isInstanceOf(ProhibitedInitialAmountException.class);
    }

    @Test
    void openAccount_should_throw_exception_on_existing_id() {
        //given
        String id = "asdasda2";
        BigDecimal amount =  BigDecimal.ONE;
        service.openAccount(id, amount);

        //then
        assertThatCode(() -> service.openAccount(id, amount)).isInstanceOf(DuplicateAccountException.class);
    }

    @Test
    void getBalance_should_return_actual_balance() {
        String id = "asdasda3";
        BigDecimal amount =  BigDecimal.ONE;
        AccountModel account = new AccountModel(id, amount);
        accounts.put(id, account);

        BigDecimal balance = service.getBalance(id);

        assertThat(balance).isEqualTo(amount);
    }
}
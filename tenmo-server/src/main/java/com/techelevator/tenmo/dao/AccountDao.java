package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.MoneyTransfer;

import java.math.BigDecimal;

public interface AccountDao {
    BigDecimal getBalanceByUserId(Long id);

    boolean sendMoney(MoneyTransfer moneyTransfer);
}

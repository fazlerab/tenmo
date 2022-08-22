package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.MoneyTransfer;
import com.techelevator.tenmo.model.TransferDetail;

import java.math.BigDecimal;

public interface AccountDao {
    BigDecimal getBalanceByUserId(Long id);

    boolean sendMoney(MoneyTransfer moneyTransfer);

    TransferDetail[] getTransfers(Long myUserId);

    TransferDetail[] getPendingTransfers(Long userId);

}

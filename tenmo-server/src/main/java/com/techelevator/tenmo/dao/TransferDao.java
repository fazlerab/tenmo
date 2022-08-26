package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.TenmoException;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;

public interface TransferDao {
    BigDecimal getBalanceByUserId(Long id);

    boolean sendMoney(Transfer transfer);

    Transfer[] getTransfers(Long myUserId);

    Transfer[] getPendingTransfers(Long userId);

    boolean approveTransfer(Transfer transfer) throws TenmoException;

    boolean rejectTransfer(Transfer transfer) throws TenmoException;

    boolean requestMoney(Transfer transfer);
}

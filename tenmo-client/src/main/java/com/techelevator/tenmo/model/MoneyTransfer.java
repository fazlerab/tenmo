package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class MoneyTransfer {
    Long transferId;
    Long transferTypeId;
    Long transferStatusId;

    Long accountFrom;

    Long accountTo;

    Long fromUserId;
    Long toUserId;
    BigDecimal amount;

    public MoneyTransfer() {
    }

    public MoneyTransfer(Long accountFrom, Long accountTo, BigDecimal amount,
                         Long transferTypeId, Long transferStatusId){
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
        this.transferStatusId = transferStatusId;
        this.transferTypeId = transferTypeId;
    }

    public MoneyTransfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getTransferTypeId() {
        return transferTypeId;
    }

    public void setTransferTypeId(Long transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public Long getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(Long transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public Long getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(Long accountFrom) {
        this.accountFrom = accountFrom;
    }

    public Long getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(Long accountTo) {
        this.accountTo = accountTo;
    }
}

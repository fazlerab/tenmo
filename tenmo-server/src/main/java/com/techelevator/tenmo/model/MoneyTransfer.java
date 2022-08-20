package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class MoneyTransfer {
    Long fromUserId;
    Long toUserId;
    BigDecimal amount;

    public MoneyTransfer() {
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
}

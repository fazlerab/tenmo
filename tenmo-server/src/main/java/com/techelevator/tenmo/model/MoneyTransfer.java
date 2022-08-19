package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class MoneyTransfer {
    Long fromUserId;
    Long toUserId;
    BigDecimal amount;

    String type;

    public MoneyTransfer() {
    }

    public MoneyTransfer(Long fromUserId, Long toUserId, BigDecimal amount, String type) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}

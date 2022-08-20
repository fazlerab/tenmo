package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferDetail {
    private Long id;
    private User fromUser;
    private User toUser;
    private String type;
    private String status;
    BigDecimal amount;

    public TransferDetail() {
    }

    public TransferDetail(Long id, User fromUser, User toUser, String type, String status, BigDecimal amount) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
        this.status = status;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

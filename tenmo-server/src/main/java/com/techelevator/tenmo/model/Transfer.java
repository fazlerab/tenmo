package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private Long id;
    private TEUser fromUser;
    private TEUser toUser;
    private String type;
    private String status;
    BigDecimal amount;

    public Transfer() {
    }

    public Transfer(Long id, TEUser fromUser, TEUser toUser, String type, String status, BigDecimal amount) {
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

    public TEUser getFromUser() {
        return fromUser;
    }

    public void setFromUser(TEUser fromUser) {
        this.fromUser = fromUser;
    }

    public TEUser getToUser() {
        return toUser;
    }

    public void setToUser(TEUser toUser) {
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

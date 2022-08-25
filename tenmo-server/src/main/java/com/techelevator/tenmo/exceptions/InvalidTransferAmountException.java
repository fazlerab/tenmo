package com.techelevator.tenmo.exceptions;

public class InvalidTransferAmountException extends TenmoException {
    public InvalidTransferAmountException(String message) {
        super(message);
    }
}

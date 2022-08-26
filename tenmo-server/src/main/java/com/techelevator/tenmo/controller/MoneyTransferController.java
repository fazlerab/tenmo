package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exceptions.TenmoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.MoneyTransfer;
import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.TransferDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class MoneyTransferController {
    @Autowired
    private final AccountDao accountDao;
    @Autowired
    private final UserDao userDao;

    public MoneyTransferController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @GetMapping("/balance/{id}")
    public BigDecimal getBalanceByUserId(@PathVariable Long id) {
        return accountDao.getBalanceByUserId(id);
    }

    @GetMapping("/otherUsers/{myId}")
    public TEUser[] getOtherUsers(@PathVariable Long myId) {
        List<TEUser> teUsers = userDao.getOtherUsers(myId);
        return teUsers.toArray(new TEUser[teUsers.size()]);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/send")
    public boolean sendMoney(@RequestBody MoneyTransfer moneyTransfer) {
        return accountDao.sendMoney(moneyTransfer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/request")
    public boolean requestMoney(@RequestBody MoneyTransfer moneyTransfer) {
        return accountDao.requestMoney(moneyTransfer);
    }

    @GetMapping("/pendings/{userId}")
    public TransferDetail[] getPendingTransfers(@PathVariable Long userId) {
        return accountDao.getPendingTransfers(userId);
    }

    @PutMapping("/approve")
    public boolean approveTransfer(@RequestBody TransferDetail transferDetail) throws TenmoException {
        return accountDao.approveTransfer(transferDetail);
    }

    @PutMapping("/reject")
    public boolean rejectTransfer(@RequestBody TransferDetail transferDetail) throws TenmoException {
        return accountDao.rejectTransfer(transferDetail);
    }

    @GetMapping("/transfers/{userId}")
    public TransferDetail[] getTransfers(@PathVariable Long userId) {
        return accountDao.getTransfers(userId);
    }

    @GetMapping("/users/{userId}/account")
    public Account list(@PathVariable long userId){
        return accountDao.getAccountByUserId(userId);
    }
}

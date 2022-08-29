package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exceptions.TenmoException;
import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {
    @Autowired
    private final TransferDao transferDao;
    @Autowired
    private final UserDao userDao;

    public TransferController(TransferDao transferDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @GetMapping("/balance/{id}")
    public BigDecimal getBalanceByUserId(@PathVariable Long id) {
        return transferDao.getBalanceByUserId(id);
    }

    @GetMapping("/otherUsers/{myUserId}")
    public TEUser[] getOtherUsers(@PathVariable Long myUserId) {
        List<TEUser> teUsers = userDao.getOtherUsers(myUserId);
        return teUsers.toArray(new TEUser[teUsers.size()]);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/send")
    public boolean sendMoney(@RequestBody Transfer transfer) throws TenmoException {
        return transferDao.sendMoney(transfer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/request")
    public boolean requestMoney(@RequestBody Transfer transfer) {
        return transferDao.requestMoney(transfer);
    }

    @GetMapping("/pendings/{userId}")
    public Transfer[] getPendingTransfers(@PathVariable Long userId) {
        return transferDao.getPendingTransfers(userId);
    }

    @PutMapping("/approve")
    public boolean approveTransfer(@RequestBody Transfer transfer) throws TenmoException {
        return transferDao.approveTransfer(transfer);
    }

    @PutMapping("/reject")
    public boolean rejectTransfer(@RequestBody Transfer transfer) throws TenmoException {
        return transferDao.rejectTransfer(transfer);
    }

    @GetMapping("/transfers/{userId}")
    public Transfer[] getTransfers(@PathVariable Long userId) {
        return transferDao.getTransfers(userId);
    }
}

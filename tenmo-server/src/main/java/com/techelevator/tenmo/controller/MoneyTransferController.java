package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}

package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.InvalidTransferAmountException;
import com.techelevator.tenmo.exceptions.TenmoException;
import com.techelevator.tenmo.exceptions.TransferFailedException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.MoneyTransfer;
import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.TransferDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalanceByUserId(Long id) {
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, id);
    }

    @Override
    public TransferDetail[] getTransfers(Long myUserId) {
        String sql = "Select transfer_id, transfer_type_id, transfer_status_id, account_from, account_to,"
                + " amount From transfer t Join account a On a.account_id = t.account_from Or a.account_id = t.account_to " +
                " Where a.user_id = ?;";

        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, myUserId);
        List<TransferDetail> transfers = new ArrayList<>();
        while(result.next()){
            transfers.add(mapToTransferDetail(result));
        }
        return transfers.toArray(new TransferDetail[transfers.size()]);
    }

    @Transactional
    @Override
    public boolean sendMoney(MoneyTransfer moneyTransfer) {
        Long transferTypeId = getTransferTypeId("Send");
        Long tranferStatusId = getTransferStatusId("Approved");

        Long fromAccountId = getAccountId(moneyTransfer.getFromUserId());
        Long toAccountId = getAccountId(moneyTransfer.getToUserId());

        BigDecimal amount = moneyTransfer.getAmount();

        insertIntoTransfer(transferTypeId, tranferStatusId, fromAccountId, toAccountId, amount);
        deductFromAccount(fromAccountId, amount);
        addToAccount(toAccountId, amount);

        return true;
    }

    public boolean requestMoney(MoneyTransfer moneyTransfer) {
        Long transferTypeId = getTransferTypeId("Request");
        Long transferStatusId = getTransferStatusId("Pending");

        Long fromAccountId = getAccountId(moneyTransfer.getFromUserId());
        Long toAccountId = getAccountId(moneyTransfer.getToUserId());

        BigDecimal amount = moneyTransfer.getAmount();

        insertIntoTransfer(transferTypeId, transferStatusId, fromAccountId, toAccountId, amount);
        return true;
    }

    @Override
    public Account getAccountByUserId(Long userId) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        if(results.next()){
            return mapRowToAccount(results);
        }
        return null;
    }

    @Override
    public TransferDetail[] getPendingTransfers(Long userId) {
        Long accountId = getAccountId(userId);
        Long transferStatusId = getTransferStatusId("Pending");
        System.out.println("accountId = " + accountId + "   statusId = " + transferStatusId);

        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer " +
                "WHERE transfer_status_id = ? " +
                "AND account_from = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferStatusId, accountId);

        List<TransferDetail> transfers = new ArrayList<>();
        while(rowSet.next()) {
            System.out.println("in while");
            transfers.add(mapToTransferDetail(rowSet));
        }

        return transfers.toArray(new TransferDetail[transfers.size()]);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean approveTransfer(TransferDetail transfer) throws TenmoException {
        BigDecimal fromUserBalance = getBalanceByUserId(transfer.getFromUser().getId());
        BigDecimal toUserBalance = getBalanceByUserId(transfer.getToUser().getId());

        if (transfer.getAmount().compareTo(fromUserBalance) > 0) {
            throw new InvalidTransferAmountException("Not enough balance to approve transfer.");
        }

        deductFromAccount(transfer.getFromUser().getId(), transfer.getAmount());
        addToAccount(transfer.getToUser().getId(), transfer.getAmount());

        Long approvedStatusId = getTransferStatusId("Approved");
        String sql = "UPDATE transfer " +
                "SET transfer_status_id = ? " +
                "WHERE transfer_id = ?;";
        int rows = jdbcTemplate.update(sql, approvedStatusId, transfer.getId());
        if (rows == 0) {
            throw new TransferFailedException("Transfer approval failed.");
        }

        return true;
    }

    @Override
    public boolean rejectTransfer(TransferDetail transfer) throws TenmoException {
        Long rejectedStatusId = getTransferStatusId("Rejected");
        String sql = "UPDATE transfer " +
                "SET transfer_status_id = ? " +
                "WHERE transfer_id = ?;";
        int rows = jdbcTemplate.update(sql, rejectedStatusId, transfer.getId());
        if (rows == 0) {
            throw new TransferFailedException("Transfer rejection failed.");
        }
        return false;
    }

    private TransferDetail mapToTransferDetail(SqlRowSet rs) {
        TransferDetail transfer = new TransferDetail();
        transfer.setId(rs.getLong("transfer_id"));
        transfer.setType(getTransferType(rs.getLong("transfer_type_id")));
        transfer.setStatus(getTransferStatus(rs.getLong("transfer_status_id")));

        Long fromAccountId = rs.getLong("account_from");
        transfer.setFromUser(getUserByAccountId(fromAccountId));

        Long toAccountId = rs.getLong("account_to");
        transfer.setToUser(getUserByAccountId(toAccountId));
        transfer.setAmount(rs.getBigDecimal("amount"));

        return transfer;
    }

    private void insertIntoTransfer(Long typeId, Long statusId, Long fromAccountId, Long toAccountId,
                                    BigDecimal amount) {
        String transferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, " +
                "account_from, account_to, amount) " +
                "VALUES(?, ?, ?, ?, ?);";
        int rows = jdbcTemplate.update(transferSql, typeId, statusId, fromAccountId,
                toAccountId, amount);
    }

    private void addToAccount(Long toAccountId, BigDecimal amount) {
        String addToUserAccountSql = "UPDATE account " +
                "SET balance = balance + ? " +
                "WHERE account_id = ?;";
        int rows = jdbcTemplate.update(addToUserAccountSql, amount, toAccountId);
    }

    private void deductFromAccount(Long accountId, BigDecimal amount) {
        String sql = "UPDATE account " +
                "SET balance = balance - ? " +
                "WHERE account_id = ?;";
        int rows = jdbcTemplate.update(sql, amount, accountId);
    }

    private Long getTransferStatusId(String status) {
        return jdbcTemplate.queryForObject(
                "SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc = ?;",
                Long.class, status);
    }

    private String getTransferStatus(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT transfer_status_desc FROM transfer_status WHERE transfer_status_id = ?;",
                String.class, id);
    }

    private Long getTransferTypeId(String type) {
        return jdbcTemplate.queryForObject(
                "SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc = ?;",
                Long.class, type);
    }

    private String getTransferType(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT transfer_type_desc FROM transfer_type WHERE transfer_type_id = ?;",
                String.class, id);
    }

    public Long getAccountId(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT account_id FROM account WHERE user_id = ?;",
                Long.class, userId);
    }

    public TEUser getUserByAccountId(Long accountId) {
        String sql = "SELECT tenmo_user.user_id, tenmo_user.username " +
                "FROM tenmo_user " +
                "JOIN account ON account.user_id = tenmo_user.user_id " +
                "WHERE account.account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        TEUser user = null;
        if (rowSet.next()) {
            user  = new TEUser(rowSet.getLong("user_id"), rowSet.getString("username"));
        }
        System.out.println("user: " + user);
        return user;
    }

    private Account mapRowToAccount(SqlRowSet results){
        Account account = new Account();
        account.setAccount_id(results.getLong("account_id"));
        account.setBalance(results.getBigDecimal("balance"));
        account.setUser_id(results.getLong("user_id"));
        return account;
    }
}

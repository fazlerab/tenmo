package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.InvalidTransferAmountException;
import com.techelevator.tenmo.exceptions.TenmoException;
import com.techelevator.tenmo.exceptions.TransferFailedException;
import com.techelevator.tenmo.model.TEUser;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalanceByUserId(Long id) {
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, id);
    }

    @Override
    public Transfer[] getTransfers(Long myUserId) {
        String sql = "Select transfer_id, transfer_type_id, transfer_status_id, account_from, account_to,"
                + " amount From transfer t Join account a On a.account_id = t.account_from Or a.account_id = t.account_to " +
                " Where a.user_id = ?;";

        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, myUserId);
        List<Transfer> transfers = new ArrayList<>();
        while(result.next()){
            transfers.add(mapToTransfer(result));
        }
        return transfers.toArray(new Transfer[transfers.size()]);
    }

    @Transactional
    @Override
    public boolean sendMoney(Transfer transfer) {
        Long transferTypeId = getTransferTypeId("Send");
        Long tranferStatusId = getTransferStatusId("Approved");

        Long fromAccountId = getAccountId(transfer.getFromUser().getId());
        Long toAccountId = getAccountId(transfer.getToUser().getId());

        BigDecimal amount = transfer.getAmount();

        insertIntoTransfer(transferTypeId, tranferStatusId, fromAccountId, toAccountId, amount);
        deductFromAccount(fromAccountId, amount);
        addToAccount(toAccountId, amount);

        return true;
    }

    @Override
    public boolean requestMoney(Transfer transfer) {
        Long transferTypeId = getTransferTypeId("Request");
        Long transferStatusId = getTransferStatusId("Pending");

        Long fromAccountId = getAccountId(transfer.getFromUser().getId());
        Long toAccountId = getAccountId(transfer.getToUser().getId());

        BigDecimal amount = transfer.getAmount();

        insertIntoTransfer(transferTypeId, transferStatusId, fromAccountId, toAccountId, amount);
        return true;
    }

    @Override
    public Transfer[] getPendingTransfers(Long userId) {
        Long accountId = getAccountId(userId);
        Long transferStatusId = getTransferStatusId("Pending");

        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer " +
                "WHERE transfer_status_id = ? " +
                "AND account_from = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferStatusId, accountId);

        List<Transfer> transfers = new ArrayList<>();
        while(rowSet.next()) {
            transfers.add(mapToTransfer(rowSet));
        }

        return transfers.toArray(new Transfer[transfers.size()]);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean approveTransfer(Transfer transfer) throws TenmoException {
        BigDecimal fromUserBalance = getBalanceByUserId(transfer.getFromUser().getId());

        if (transfer.getAmount().compareTo(fromUserBalance) > 0) {
            throw new InvalidTransferAmountException("Not enough balance to approve transfer.");
        }

        Long fromAccountId = getAccountId(transfer.getFromUser().getId());
        Long toAccountId = getAccountId(transfer.getToUser().getId());

        deductFromAccount(fromAccountId, transfer.getAmount());
        addToAccount(toAccountId, transfer.getAmount());

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
    public boolean rejectTransfer(Transfer transfer) throws TenmoException {
        Long rejectedStatusId = getTransferStatusId("Rejected");

        String sql = "UPDATE transfer " +
                "SET transfer_status_id = ? " +
                "WHERE transfer_id = ?;";
        int rows = jdbcTemplate.update(sql, rejectedStatusId, transfer.getId());
        if (rows == 0) {
            throw new TransferFailedException("Transfer rejection failed.");
        }
        return true;
    }

    private Transfer mapToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
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

    private Long getAccountId(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT account_id FROM account WHERE user_id = ?;",
                Long.class, userId);
    }

    private TEUser getUserByAccountId(Long accountId) {
        String sql = "SELECT tenmo_user.user_id, tenmo_user.username " +
                "FROM tenmo_user " +
                "JOIN account ON account.user_id = tenmo_user.user_id " +
                "WHERE account.account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        TEUser user = null;
        if (rowSet.next()) {
            user  = new TEUser(rowSet.getLong("user_id"), rowSet.getString("username"));
        }
        return user;
    }
}

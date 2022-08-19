package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.MoneyTransfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

    @Transactional
    @Override
    public boolean sendMoney(MoneyTransfer moneyTransfer) {
        Long transferTypeId = jdbcTemplate.queryForObject(
                "SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc = 'Send'",
                Long.class);

        Long tranferStatusId = jdbcTemplate.queryForObject(
                "SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc = 'Approved';",
                Long.class);

        Long accountFromId = jdbcTemplate.queryForObject(
                "SELECT account_id FROM account WHERE user_id = ?;",
                Long.class, moneyTransfer.getFromUserId());

        Long accountToId = jdbcTemplate.queryForObject(
                "SELECT account_id FROM account WHERE user_id = ?;",
                Long.class, moneyTransfer.getToUserId());

        BigDecimal amount = moneyTransfer.getAmount();

        String transferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, " +
                "account_from, account_to, amount) " +
                "VALUES(?, ?, ?, ?, ?);";
        int rows1 = jdbcTemplate.update(transferSql, transferTypeId, tranferStatusId, accountFromId,
                accountToId, amount);

        String deductFromUserAccountSql = "UPDATE account " +
                "SET balance = balance - ? " +
                "WHERE account_id = ?;";
        int rows2 = jdbcTemplate.update(deductFromUserAccountSql, amount, accountFromId);

        String addToUserAccountSql = "UPDATE account " +
                "SET balance = balance + ? " +
                "WHERE account_id = ?;";
        int rows3 = jdbcTemplate.update(addToUserAccountSql, amount, accountToId);

        return true;
    }
}

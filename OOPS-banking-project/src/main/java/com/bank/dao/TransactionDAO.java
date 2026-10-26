package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insertTransaction(Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_id, account_number, counterparty_account, type, amount, balance_after) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.getTransactionId());
            pstmt.setString(2, tx.getAccountNumber());
            pstmt.setString(3, tx.getCounterpartyAccount());
            pstmt.setString(4, tx.getType());
            pstmt.setDouble(5, tx.getAmount());
            pstmt.setDouble(6, tx.getBalanceAfter());
            pstmt.executeUpdate();
        }
    }

    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getString("transaction_id"),
                        rs.getString("account_number"),
                        rs.getString("counterparty_account"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getTimestamp("timestamp")
                ));
            }
        }
        return list;
    }
}

package com.bank.dao;

import com.bank.model.Account;
import com.bank.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public Account findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(
                        rs.getString("account_number"),
                        rs.getString("customer_id"),
                        rs.getString("branch_id"),
                        rs.getString("account_type"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                );
            }
        }
        return null;
    }

    public List<Account> getAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_number";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accounts.add(new Account(
                        rs.getString("account_number"),
                        rs.getString("customer_id"),
                        rs.getString("branch_id"),
                        rs.getString("account_type"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                ));
            }
        }
        return accounts;
    }

    public List<String[]> getCustomerAccountSummaries() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT * FROM v_customer_account_summary";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("account_number"),
                        rs.getString("full_name"),
                        rs.getString("account_type"),
                        String.valueOf(rs.getDouble("balance"))
                });
            }
        }
        return list;
    }
}

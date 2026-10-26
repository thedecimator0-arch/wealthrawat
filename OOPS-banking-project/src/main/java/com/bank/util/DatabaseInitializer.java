package com.bank.util;

import java.sql.*;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // Branches
            stmt.execute("CREATE TABLE IF NOT EXISTS branches (" +
                    "branch_id VARCHAR(10) PRIMARY KEY," +
                    "ifsc_code VARCHAR(11) UNIQUE NOT NULL," +
                    "branch_name VARCHAR(100) NOT NULL," +
                    "city VARCHAR(50) NOT NULL" +
                    ");");

            // Customers
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id VARCHAR(10) PRIMARY KEY," +
                    "full_name VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100) UNIQUE NOT NULL," +
                    "phone VARCHAR(15) UNIQUE NOT NULL" +
                    ");");

            // Accounts
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number VARCHAR(20) PRIMARY KEY," +
                    "customer_id VARCHAR(10) NOT NULL," +
                    "branch_id VARCHAR(10) NOT NULL," +
                    "account_type VARCHAR(20) CHECK (account_type IN ('SAVINGS', 'CURRENT'))," +
                    "balance DECIMAL(15, 2) DEFAULT 0.00," +
                    "status VARCHAR(10) DEFAULT 'ACTIVE'," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (branch_id) REFERENCES branches(branch_id)" +
                    ");");

            // Transactions
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id VARCHAR(20) PRIMARY KEY," +
                    "account_number VARCHAR(20) NOT NULL," +
                    "counterparty_account VARCHAR(20)," +
                    "type VARCHAR(15) CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT'))," +
                    "amount DECIMAL(15, 2) NOT NULL," +
                    "balance_after DECIMAL(15, 2) NOT NULL," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                    ");");

            // View
            stmt.execute("DROP VIEW IF EXISTS v_customer_account_summary;");
            stmt.execute("CREATE VIEW v_customer_account_summary AS " +
                    "SELECT c.customer_id, c.full_name, c.email, a.account_number, a.account_type, a.balance " +
                    "FROM customers c JOIN accounts a ON c.customer_id = a.customer_id;");

            // Trigger
            stmt.execute("DROP TRIGGER IF EXISTS update_account_balance_after_transaction;");
            stmt.execute("CREATE TRIGGER update_account_balance_after_transaction " +
                    "AFTER INSERT ON transactions " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "UPDATE accounts SET balance = NEW.balance_after WHERE account_number = NEW.account_number; " +
                    "END;");

            // Seed Data
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO branches VALUES ('BR-001', 'APEX0001001', 'Connaught Place Branch', 'New Delhi');");
                    stmt.execute("INSERT INTO customers VALUES ('CUST-101', 'Aarav Sharma', 'aarav.sharma@apex.in', '+919876543210');");
                    stmt.execute("INSERT INTO customers VALUES ('CUST-102', 'Priya Patel', 'priya.patel@apex.in', '+919876543211');");
                    stmt.execute("INSERT INTO accounts VALUES ('ACC-1001', 'CUST-101', 'BR-001', 'SAVINGS', 345000.00, 'ACTIVE');");
                    stmt.execute("INSERT INTO accounts VALUES ('ACC-1002', 'CUST-102', 'BR-001', 'CURRENT', 1245800.00, 'ACTIVE');");
                    System.out.println("[INIT] Seed data populated in database.");
                }
            }

        }
    }
}

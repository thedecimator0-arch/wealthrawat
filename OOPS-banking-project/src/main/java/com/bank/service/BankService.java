package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class BankService {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void displayAccountsFromView() throws SQLException {
        List<String[]> summaries = accountDAO.getCustomerAccountSummaries();
        System.out.println("\n=================================================================");
        System.out.println("           CUSTOMER ACCOUNTS OVERVIEW (SQL VIEW SOURCE)           ");
        System.out.println("=================================================================");
        System.out.printf("%-12s | %-18s | %-10s | %-15s%n", "Account No", "Customer Name", "Type", "Balance (INR)");
        System.out.println("-----------------------------------------------------------------");
        for (String[] row : summaries) {
            double bal = Double.parseDouble(row[3]);
            System.out.printf("%-12s | %-18s | %-10s | ₹%,14.2f%n", row[0], row[1], row[2], bal);
        }
        System.out.println("-----------------------------------------------------------------");
    }

    public void deposit(String accountNumber, double amount) throws SQLException {
        Transaction tx = depositToAccount(accountNumber, amount);

        System.out.println("\n[DEPOSIT SUCCESSFUL]");
        System.out.println(" - Reference ID : " + tx.getTransactionId());
        System.out.println(" - Account      : " + accountNumber);
        System.out.println(" - Amount       : ₹" + String.format("%,.2f", amount));
        System.out.println("\n🔥 [SQL TRIGGER EXECUTED] Trigger 'update_account_balance_after_transaction'");
        System.out.println("   Updated accounts.balance automatically to ₹" + String.format("%,.2f", tx.getBalanceAfter()));
    }

    public List<Account> getAccounts() throws SQLException {
        return accountDAO.getAll();
    }

    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        return transactionDAO.getRecentTransactions(limit);
    }

    public Transaction depositToAccount(String accountNumber, double amount) throws SQLException {
        requirePositiveAmount(amount);
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!lockActiveAccount(conn, accountNumber)) {
                    throw new IllegalArgumentException("Account not found or inactive.");
                }
                double newBalance = getBalance(conn, accountNumber) + amount;
                Transaction transaction = new Transaction(
                        transactionId(), accountNumber, null, "DEPOSIT", amount, newBalance, null);
                insertTransaction(conn, transaction);
                conn.commit();
                return transaction;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void transfer(String senderAccNum, String recipientAccNum, double amount) throws SQLException {
        transferFunds(senderAccNum, recipientAccNum, amount);
        Account sender = accountDAO.findByAccountNumber(senderAccNum);
        Account recipient = accountDAO.findByAccountNumber(recipientAccNum);
        System.out.println("\n[TRANSFER SUCCESSFUL]");
        System.out.println(" - Transferred ₹" + String.format("%,.2f", amount) + " from " + senderAccNum + " to " + recipientAccNum);
        System.out.println(" - Sender New Balance   : ₹" + String.format("%,.2f", sender.getBalance()));
        System.out.println(" - Recipient New Balance: ₹" + String.format("%,.2f", recipient.getBalance()));
        System.out.println("\n🔥 [SQL TRIGGERS EXECUTED] Dual triggers auto-calculated both balances!");
    }

    public void transferFunds(String senderAccNum, String recipientAccNum, double amount) throws SQLException {
        requirePositiveAmount(amount);
        if (senderAccNum.equals(recipientAccNum)) {
            throw new IllegalArgumentException("Choose two different accounts.");
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!lockActiveAccount(conn, senderAccNum) || !lockActiveAccount(conn, recipientAccNum)) {
                    throw new IllegalArgumentException("Sender or recipient account not found or inactive.");
                }
                double senderBalance = getBalance(conn, senderAccNum);
                double recipientBalance = getBalance(conn, recipientAccNum);
                if (senderBalance < amount) {
                    throw new IllegalArgumentException("Insufficient funds.");
                }
                insertTransaction(conn, new Transaction(
                        transactionId(), senderAccNum, recipientAccNum, "TRANSFER_OUT",
                        amount, senderBalance - amount, null));
                insertTransaction(conn, new Transaction(
                        transactionId(), recipientAccNum, senderAccNum, "TRANSFER_IN",
                        amount, recipientBalance + amount, null));
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void requirePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be a finite number greater than zero.");
        }
    }

    private static boolean lockActiveAccount(Connection conn, String accountNumber) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE accounts SET balance = balance WHERE account_number = ? AND status = 'ACTIVE'")) {
            statement.setString(1, accountNumber);
            return statement.executeUpdate() == 1;
        }
    }

    private static double getBalance(Connection conn, String accountNumber) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_number = ?")) {
            statement.setString(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Account not found.");
                }
                return result.getDouble("balance");
            }
        }
    }

    private static void insertTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_id, account_number, counterparty_account, type, amount, balance_after) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionId());
            statement.setString(2, transaction.getAccountNumber());
            statement.setString(3, transaction.getCounterpartyAccount());
            statement.setString(4, transaction.getType());
            statement.setDouble(5, transaction.getAmount());
            statement.setDouble(6, transaction.getBalanceAfter());
            statement.executeUpdate();
        }
    }

    private static String transactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public void displayTransactions() throws SQLException {
        List<Transaction> list = transactionDAO.getRecentTransactions(10);
        System.out.println("\n=================================================================");
        System.out.println("                   AUDIT LOG (RECENT TRANSACTIONS)               ");
        System.out.println("=================================================================");
        System.out.printf("%-12s | %-10s | %-13s | %-12s | %-19s%n", "Tx ID", "Account", "Type", "Amount", "Timestamp");
        System.out.println("-----------------------------------------------------------------");
        for (Transaction tx : list) {
            System.out.printf("%-12s | %-10s | %-13s | ₹%,10.2f | %-19s%n",
                    tx.getTransactionId(),
                    tx.getAccountNumber(),
                    tx.getType(),
                    tx.getAmount(),
                    tx.getTimestamp());
        }
        System.out.println("-----------------------------------------------------------------");
    }

    public void inspectMetadata() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("\n=================================================================");
            System.out.println("               ACTIVE DATABASE VIEWS & TRIGGERS                  ");
            System.out.println("=================================================================");
            
            System.out.println("\n1. ACTIVE VIEWS:");
            ResultSet rsV = stmt.executeQuery("SELECT name, sql FROM sqlite_master WHERE type='view'");
            while (rsV.next()) {
                System.out.println(" - " + rsV.getString("name") + " : " + rsV.getString("sql"));
            }

            System.out.println("\n2. ACTIVE TRIGGERS:");
            ResultSet rsT = stmt.executeQuery("SELECT name, sql FROM sqlite_master WHERE type='trigger'");
            while (rsT.next()) {
                System.out.println(" - " + rsT.getString("name") + " : " + rsT.getString("sql"));
            }
        }
    }
}

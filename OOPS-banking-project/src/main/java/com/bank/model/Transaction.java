package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private String counterpartyAccount;
    private String type;
    private double amount;
    private double balanceAfter;
    private Timestamp timestamp;

    public Transaction() {}

    public Transaction(String transactionId, String accountNumber, String counterpartyAccount, String type, double amount, double balanceAfter, Timestamp timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.counterpartyAccount = counterpartyAccount;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCounterpartyAccount() { return counterpartyAccount; }
    public void setCounterpartyAccount(String counterpartyAccount) { this.counterpartyAccount = counterpartyAccount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}

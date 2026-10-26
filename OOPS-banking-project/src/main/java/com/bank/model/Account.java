package com.bank.model;

public class Account {
    private String accountNumber;
    private String customerId;
    private String branchId;
    private String accountType;
    private double balance;
    private String status;

    public Account() {}

    public Account(String accountNumber, String customerId, String branchId, String accountType, double balance, String status) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.branchId = branchId;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

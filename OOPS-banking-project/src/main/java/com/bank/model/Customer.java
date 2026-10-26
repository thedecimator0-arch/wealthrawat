package com.bank.model;

public class Customer {
    private String customerId;
    private String fullName;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(String customerId, String fullName, String email, String phone) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

-- ============================================================
-- APEX BANKING SYSTEM - COMPLETE SQL SCHEMA & TRIGGERS
-- ============================================================

CREATE TABLE IF NOT EXISTS branches (
    branch_id VARCHAR(10) PRIMARY KEY,
    ifsc_code VARCHAR(11) UNIQUE NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(10) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(10) NOT NULL,
    branch_id VARCHAR(10) NOT NULL,
    account_type VARCHAR(20) CHECK (account_type IN ('SAVINGS', 'CURRENT')),
    balance DECIMAL(15, 2) DEFAULT 0.00,
    status VARCHAR(10) DEFAULT 'ACTIVE',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(20) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    counterparty_account VARCHAR(20),
    type VARCHAR(15) CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')),
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

CREATE VIEW IF NOT EXISTS v_customer_account_summary AS
SELECT 
    c.customer_id,
    c.full_name,
    c.email,
    a.account_number,
    a.account_type,
    a.balance
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id;

CREATE TRIGGER IF NOT EXISTS update_account_balance_after_transaction
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    UPDATE accounts
    SET balance = NEW.balance_after
    WHERE account_number = NEW.account_number;
END;

-- Temporary sample data (run once after creating the tables)
INSERT INTO branches VALUES ('BR-002', 'APEX0001002', 'Sample Branch', 'Mumbai');
INSERT INTO customers VALUES ('CUST-103', 'Riya Shah', 'riya.shah@example.com', '9876543212');
INSERT INTO accounts VALUES ('ACC-1003', 'CUST-103', 'BR-002', 'SAVINGS', 5000.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-001', 'ACC-1003', 'DEPOSIT', 5000.00, 5000.00);

INSERT INTO branches VALUES ('BR-003', 'APEX0001003', 'Park Street Branch', 'Kolkata');
INSERT INTO customers VALUES ('CUST-104', 'Kabir Mehta', 'kabir.mehta@example.com', '9876543213');
INSERT INTO accounts VALUES ('ACC-1004', 'CUST-104', 'BR-003', 'CURRENT', 12000.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-002', 'ACC-1004', 'DEPOSIT', 12000.00, 12000.00);

INSERT INTO customers VALUES ('CUST-105', 'Anaya Rao', 'anaya.rao@example.com', '9876543214');
INSERT INTO accounts VALUES ('ACC-1005', 'CUST-105', 'BR-002', 'SAVINGS', 7500.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-003', 'ACC-1005', 'DEPOSIT', 7500.00, 7500.00);

INSERT INTO customers VALUES ('CUST-106', 'Dev Patel', 'dev.patel@example.com', '9876543215');
INSERT INTO accounts VALUES ('ACC-1006', 'CUST-106', 'BR-003', 'SAVINGS', 9300.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-004', 'ACC-1006', 'DEPOSIT', 9300.00, 9300.00);

INSERT INTO customers VALUES ('CUST-107', 'Meera Nair', 'meera.nair@example.com', '9876543216');
INSERT INTO accounts VALUES ('ACC-1007', 'CUST-107', 'BR-002', 'CURRENT', 15000.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-005', 'ACC-1007', 'DEPOSIT', 15000.00, 15000.00);

INSERT INTO customers VALUES ('CUST-108', 'Arjun Singh', 'arjun.singh@example.com', '9876543217');
INSERT INTO accounts VALUES ('ACC-1008', 'CUST-108', 'BR-003', 'SAVINGS', 8200.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-006', 'ACC-1008', 'DEPOSIT', 8200.00, 8200.00);

INSERT INTO customers VALUES ('CUST-109', 'Sara Khan', 'sara.khan@example.com', '9876543218');
INSERT INTO accounts VALUES ('ACC-1009', 'CUST-109', 'BR-002', 'SAVINGS', 6400.00, 'ACTIVE');
INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after)
VALUES ('TXN-007', 'ACC-1009', 'DEPOSIT', 6400.00, 6400.00);

-- Basic queries to view the sample data
SELECT * FROM customers;
SELECT * FROM accounts;
SELECT * FROM transactions;

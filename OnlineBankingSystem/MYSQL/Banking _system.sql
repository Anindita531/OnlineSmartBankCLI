CREATE DATABASE bank_system;
USE bank_system;

CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0,
    password VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE account_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL,
    password VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    type ENUM('DEPOSIT','WITHDRAW','INTEREST') NOT NULL,
    amount DOUBLE NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

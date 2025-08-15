# 🏦 Secure Online Banking System

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)  
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)  
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

A **console-based Java banking application** with MySQL integration, email notifications, transaction history, and interest management.

---

## **Overview**

Secure Online Banking System simulates real banking operations:

- Customer account creation, login, deposits, withdrawals  
- Admin approval/rejection of accounts  
- Transaction tracking including deposits, withdrawals, and interest  
- Email notifications on transactions and interest additions

---

## **Features**

### **Admin**
- View pending account requests  
- Approve or reject account requests  
- View transaction history of any account  
- Add interest to accounts

### **Customer**
- Request new account  
- Login with account ID and password  
- View balance  
- Deposit and withdraw money  
- View transaction history  
- Receive email notifications for all transactions  

---

## **Technologies Used**

- **Java 17**  
- **MySQL 8.0**  
- **JDBC**  
- **BCrypt** for password hashing  
- **JavaMail API** for email notifications  
- **JUnit 5** for unit testing  
- **Maven** for dependency and build management  
2. Tables
accounts
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0,
    password VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL
);

account_requests
CREATE TABLE account_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL,
    password VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL
);

transactions
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    type ENUM('DEPOSIT','WITHDRAW','INTEREST') NOT NULL,
    amount DOUBLE NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

Project Structure
com.bank
│
├── Main.java            // Main application entry point
├── BankService.java     // Customer banking operations
├── AdminService.java    // Admin functionalities
├── CustomerService.java // Customer account requests
├── EmailUtil.java       // Email sending utility
├── DBConnection.java    // Database connection
└── BankServiceTest.java // Unit tests

Setup Instructions
1. Database Configuration

Update DBConnection.java with your MySQL username, password, and database URL.

2. Email Configuration

Update EmailUtil.java with Gmail account credentials (use App Password for Gmail):

final String fromEmail = "yourbankemail@gmail.com";
final String password = "yourAppPassword";

3. Running the Application
# Compile and run
mvn compile
mvn exec:java -Dexec.mainClass="com.bank.Main"


Console-based menu will guide you for Admin and Customer operations.

Usage Example
Customer Flow
--- Welcome to Secure Online Banking ---
1. Login as Admin
2. Login as Customer
3. Request New Account
4. Exit
Choose option: 3
Enter name: John Doe
Enter initial deposit: 500
Enter password: jd123
Enter email: john@example.com
Account request submitted successfully!

Admin Flow
--- Admin Menu ---
1. View Pending Requests
2. Approve Request
3. Reject Request
4. View Transactions of an Account
5. Add Interest to an Account
6. Logout
Choose option: 1
Pending Account Requests:
ID: 1 | Name: John Doe | Initial Deposit: 500

Testing

Unit tests implemented with JUnit 5

# Run tests
mvn test


Make sure email and database are properly configured before testing.

Future Enhancements

GUI interface with JavaFX/Swing

SMS notifications using external APIs

Multi-currency support

Scheduled automated interest calculations

Analytics dashboard for Admin
---

## **Database Setup**

### **1. Create Database**
```sql
CREATE DATABASE bank_system;
USE bank_system;

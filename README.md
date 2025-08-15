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

---

## **Database Setup**

### **1. Create Database**
```sql
CREATE DATABASE bank_system;
USE bank_system;

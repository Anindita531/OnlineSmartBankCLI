package com.bank;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class BankService {

    public boolean login(int accountId, String password) {
        final String sql =
            "SELECT account_holder_name, password FROM accounts WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Invalid Account ID or Password! Exiting...");
                    return false;
                }

                String holder = rs.getString("account_holder_name");
                String storedHash = rs.getString("password");

                boolean ok = storedHash != null && BCrypt.checkpw(password, storedHash);
                if (ok) {
                    System.out.println("Login successful! Welcome " + holder);
                    return true;
                } else {
                    System.out.println("Invalid Account ID or Password! Exiting...");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

   public void addInterest(int accountId, double rate) {
    final String sql = "UPDATE accounts SET balance = balance + (balance * ? / 100) WHERE account_id = ?";
    final String selectSql = "SELECT account_holder_name, email, balance FROM accounts WHERE account_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         PreparedStatement selectPs = conn.prepareStatement(selectSql);
         PreparedStatement transPs = conn.prepareStatement(
             "INSERT INTO transactions (account_id, type, amount) VALUES (?, 'INTEREST', ?)"
         )) {

        // Get current balance and user info
        selectPs.setInt(1, accountId);
        try (ResultSet rs = selectPs.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Account not found!");
                return;
            }
            String name = rs.getString("account_holder_name");
            String email = rs.getString("email");
            double balance = rs.getDouble("balance");

            // Calculate interest amount
            double interestAmount = balance * rate / 100;

            // Update balance
            ps.setDouble(1, rate);
            ps.setInt(2, accountId);
            ps.executeUpdate();

            // Insert transaction record
            transPs.setInt(1, accountId);
            transPs.setDouble(2, interestAmount);
            transPs.executeUpdate();

            System.out.println("Interest added successfully! Amount: " + interestAmount);

            // Send email notification
            String message = "Hello " + name + ",\n\n" +
                             "Interest Added!\n" +
                             "Rate: " + rate + "%\n" +
                             "Interest Amount: " + interestAmount + "\n" +
                             "Current Balance: " + (balance + interestAmount) + "\n\n" +
                             "Regards,\nSecure Bank";

            EmailUtil.sendEmail(email, "Interest Credited", message);
        }

    } catch (Exception e) {
        if (e.getMessage() != null) {
            System.err.println("Error adding interest: " + e.getMessage());
        } else {
            System.err.println("An error occurred while adding interest.");
            
        }
    }
}

    // View balance
    public void viewBalance(int accountId) {
        final String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Balance: " + rs.getDouble("balance"));
                } else {
                    System.out.println("Account not found!");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Deposit money
    public void deposit(int accountId, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid deposit amount. Must be positive.");
            return;
        }

        final String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        final String selectSql = "SELECT account_holder_name, email, balance FROM accounts WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement updatePs = conn.prepareStatement(updateSql);
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            conn.setAutoCommit(false);

            // Update balance
            updatePs.setDouble(1, amount);
            updatePs.setInt(2, accountId);
            int rows = updatePs.executeUpdate();
            if (rows == 0) {
                System.out.println("Account not found!");
                conn.rollback();
                return;
            }

            // Insert transaction
            String txnSql = "INSERT INTO transactions (account_id, type, amount) VALUES (?, 'DEPOSIT', ?)";
            try (PreparedStatement txnPs = conn.prepareStatement(txnSql)) {
                txnPs.setInt(1, accountId);
                txnPs.setDouble(2, amount);
                txnPs.executeUpdate();
            }

            // Get email info
            selectPs.setInt(1, accountId);
            String name = "", email = "";
            double balance = 0;
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("account_holder_name");
                    email = rs.getString("email");
                    balance = rs.getDouble("balance");
                }
            }

            conn.commit();
            System.out.println("Deposited successfully!");

            // Send email
            String message = "Hello " + name + ",\n\n" +
                    "Deposit Successful!\n" +
                    "Amount: " + amount + "\n" +
                    "Current Balance: " + balance + "\n\n" +
                    "Regards,\nSecure Bank";

            EmailUtil.sendEmail(email, "Deposit Confirmation", message);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // Withdraw money
    public void withdraw(int accountId, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount. Must be positive.");
            return;
        }

        final String selectSql = "SELECT account_holder_name, email, balance FROM accounts WHERE account_id = ? FOR UPDATE";
        final String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql);
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

            conn.setAutoCommit(false);

            selectPs.setInt(1, accountId);
            String name = "", email = "";
            double balance = 0;
            try (ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Account not found!");
                    conn.rollback();
                    return;
                }
                balance = rs.getDouble("balance");
                if (balance < amount) {
                    System.out.println("Insufficient balance!");
                    conn.rollback();
                    return;
                }
                name = rs.getString("account_holder_name");
                email = rs.getString("email");
            }

            // Update balance
            updatePs.setDouble(1, amount);
            updatePs.setInt(2, accountId);
            updatePs.executeUpdate();

            // Insert transaction
            String txnSql = "INSERT INTO transactions (account_id, type, amount) VALUES (?, 'WITHDRAW', ?)";
            try (PreparedStatement txnPs = conn.prepareStatement(txnSql)) {
                txnPs.setInt(1, accountId);
                txnPs.setDouble(2, amount);
                txnPs.executeUpdate();
            }

            conn.commit();
            System.out.println("Withdrawal successful!");

            // Send email
            String message = "Hello " + name + ",\n\n" +
                    "Withdrawal Successful!\n" +
                    "Amount: " + amount + "\n" +
                    "Current Balance: " + (balance - amount) + "\n\n" +
                    "Regards,\nSecure Bank";

            EmailUtil.sendEmail(email, "Withdrawal Confirmation", message);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // View transaction history
    public void viewTransactions(int accountId) {
        final String sql = "SELECT type, amount, transaction_date FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Transaction History:");
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("%s: %.2f on %s%n",
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("transaction_date"));
                }
                if (!any) System.out.println("(No transactions yet)");
            }
        } catch (Exception e) { 
            
            if (e.getMessage() != null ){
                System.err.println("Error viewing transactions: " + e.getMessage());
            } else {
                e.printStackTrace(); 
                System.out.println("An error occurred while viewing transactions.");
            
            }
        }
    }


   
}

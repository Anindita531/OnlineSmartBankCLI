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

 public void deposit(int accountId, double amount) {
    if (amount <= 0) {
        System.out.println("Invalid deposit amount. Must be positive.");
        return;
    }

    final String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
    final String emailSql = "SELECT account_holder_name, email, balance FROM accounts WHERE account_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         PreparedStatement emailPs = conn.prepareStatement(emailSql)) {

        ps.setDouble(1, amount);
        ps.setInt(2, accountId);
        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Deposited successfully!");

            // Get email and balance
            emailPs.setInt(1, accountId);
            try (ResultSet rs = emailPs.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("account_holder_name");
                    String email = rs.getString("email");
                    double balance = rs.getDouble("balance");

                    String message = "Hello " + name + ",\n\n" +
                                     "Deposit Successful!\n" +
                                     "Amount: " + amount + "\n" +
                                     "Current Balance: " + balance + "\n\n" +
                                     "Regards,\nSecure Bank";

                    EmailUtil.sendEmail(email, "Deposit Confirmation", message);
                }
            }
        } else {
            System.out.println("Account not found!");
        }
    } catch (Exception e) { e.printStackTrace(); }
}

public void withdraw(int accountId, double amount) {
    if (amount <= 0) {
        System.out.println("Invalid withdrawal amount. Must be positive.");
        return;
    }

    final String getSql = "SELECT account_holder_name, email, balance FROM accounts WHERE account_id = ? FOR UPDATE";
    final String updSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement getPs = conn.prepareStatement(getSql);
         PreparedStatement updPs = conn.prepareStatement(updSql)) {

        conn.setAutoCommit(false);

        getPs.setInt(1, accountId);
        try (ResultSet rs = getPs.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Account not found!");
                conn.rollback();
                return;
            }

            double balance = rs.getDouble("balance");
            if (balance < amount) {
                System.out.println("Insufficient balance!");
                conn.rollback();
                return;
            }

            String name = rs.getString("account_holder_name");
            String email = rs.getString("email");

            updPs.setDouble(1, amount);
            updPs.setInt(2, accountId);
            updPs.executeUpdate();

            conn.commit();
            System.out.println("Withdrawal successful!");

            String message = "Hello " + name + ",\n\n" +
                             "Withdrawal Successful!\n" +
                             "Amount: " + amount + "\n" +
                             "Current Balance: " + (balance - amount) + "\n\n" +
                             "Regards,\nSecure Bank";

            EmailUtil.sendEmail(email, "Withdrawal Confirmation", message);

        }
    } catch (Exception e) { e.printStackTrace(); }
}

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
        } catch (Exception e) { e.printStackTrace(); }
    }
}

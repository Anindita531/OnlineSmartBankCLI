package com.bank;

import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import org.mindrot.jbcrypt.BCrypt;

public class AdminService {

    public boolean adminLogin(String username, String password) {
        final String sql = "SELECT username, password FROM users WHERE username=? AND role='ADMIN' AND status='ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Invalid username or password!");
                    return false;
                }

                String storedHash = rs.getString("password");
                boolean ok = storedHash != null && BCrypt.checkpw(password, storedHash);
                if (ok) {
                    System.out.println("Admin login successful!");
                    return true;
                } else {
                    System.out.println("Invalid username or password!");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void viewAccountRequests() {
        final String sql = "SELECT request_id, name, deposit, status FROM account_requests WHERE status='PENDING' ORDER BY request_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Pending Account Requests:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("ID: %d, Name: %s, Deposit: %.2f, Status: %s%n",
                        rs.getInt("request_id"),
                        rs.getString("name"),
                        rs.getDouble("deposit"),
                        rs.getString("status"));
            }
            if (!any) System.out.println("(none)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void approveAccount(int requestId) {
        final String getSql = "SELECT name, deposit, password, email FROM account_requests WHERE request_id=? AND status='PENDING'";
        final String insertSql = "INSERT INTO accounts (account_holder_name, balance, password) VALUES (?, ?, ?)";
        final String updateSql = "UPDATE account_requests SET status='APPROVED' WHERE request_id=?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement getPs = conn.prepareStatement(getSql)) {
                getPs.setInt(1, requestId);
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Request not found or not pending!");
                        conn.rollback();
                        return;
                    }

                    String name = rs.getString("name");
                    double deposit = rs.getDouble("deposit");
                    String hashedPwd = rs.getString("password");
                    String email = rs.getString("email");

                    try (PreparedStatement insPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                         PreparedStatement updPs = conn.prepareStatement(updateSql)) {

                        insPs.setString(1, name);
                        insPs.setDouble(2, deposit);
                        insPs.setString(3, hashedPwd);
                        insPs.executeUpdate();

                        ResultSet keys = insPs.getGeneratedKeys();
                        int accountId = -1;
                        if (keys.next()) accountId = keys.getInt(1);

                        updPs.setInt(1, requestId);
                        updPs.executeUpdate();

                        conn.commit();
                        System.out.println("Account request approved.");
                        sendEmail(email, accountId);
                    }
                }
            } catch (Exception inner) {
                conn.rollback();
                throw inner;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rejectAccount(int requestId) {
        final String sql = "UPDATE account_requests SET status='REJECTED' WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            int n = ps.executeUpdate();
            if (n > 0) System.out.println("Account request rejected.");
            else System.out.println("Request not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(String toEmail, int accountId) {
        final String fromEmail = "ganindita058@gmail.com"; // sender email
        final String password = "lmfetkbateuacxiv"; // app-specific password if using Gmail

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
          
            String mesage = "Hello "  + ",\n\n" +
                 "Your account has been approved!\n" +
                 "Your Account ID is: " + accountId + "\n\n" +
                 "Regards,\nSecure Bank";


            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Bank Account is Approved!");
            message.setText("Congratulations! Your account is approved.\n: " +mesage);
            Transport.send(message);
            System.out.println("Email sent successfully to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
     public void viewAccountTransactions(int accountId) {
        final String sql = "SELECT transaction_id, type, amount, transaction_date " +
                           "FROM transactions WHERE account_id = ? ORDER BY transaction_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Transactions for account ID: " + accountId);
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("ID: %d | Type: %s | Amount: %.2f | Date: %s%n",
                            rs.getInt("transaction_id"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("transaction_date"));
                }
                if (!any) {
                    System.out.println("(No transactions found)");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

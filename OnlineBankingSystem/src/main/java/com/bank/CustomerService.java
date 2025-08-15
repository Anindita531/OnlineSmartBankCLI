package com.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class CustomerService {

    private Scanner sc = new Scanner(System.in);

    /**
     * Request a new account.
     * This will insert a pending request into account_requests table.
     */
    public void requestNewAccount() {
        try {
            System.out.print("Enter your name: ");
            String name = sc.nextLine();

            System.out.print("Enter initial deposit: ");
            double deposit = sc.nextDouble();
            sc.nextLine(); // consume newline

            System.out.print("Enter your email: ");
            String email = sc.nextLine();

            System.out.print("Enter password: ");
            String password = sc.nextLine();

            // Hash the password using SecurityUtil
            String hashedPwd = SecurityUtil.hashPassword(password);

            // Insert into account_requests
            String sql = "INSERT INTO account_requests (name, deposit, password, email, status) VALUES (?, ?, ?, ?, 'PENDING')";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setDouble(2, deposit);
                ps.setString(3, hashedPwd);
                ps.setString(4, email);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Account request submitted successfully! Your request is pending approval.");
                    System.out.println("You will receive your account ID via email once approved.");
                } else {
                    System.out.println("Failed to submit account request.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while requesting a new account.";
            if (e.getMessage() != null) {
                errorMessage += " Error details: " + e.getMessage();
            }       
           
        }
    }
}

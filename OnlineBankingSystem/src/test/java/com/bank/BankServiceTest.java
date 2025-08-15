package com.bank;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankServiceTest {

    private BankService bankService;
    private int testAccountId = 1; // must exist in DB

    @BeforeAll
    public void setUpAll() {
        // Ensure test account exists before all tests
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/banking_system",
                "root",
                "Welcome2025!");
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM accounts WHERE account_id = " + testAccountId);
            stmt.executeUpdate(
                    "INSERT INTO accounts (account_id, account_holder_name, balance, password) " +
                            "VALUES (" + testAccountId + ", 'JUnit Test User', 1000, 'testpass')"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Failed to set up test account");
        }
    }

    @BeforeEach
    public void setUp() {
        bankService = new BankService();
        // Reset account balance before each test
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/banking_system",
                "root",
                "Welcome2025!");
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("UPDATE accounts SET balance = 1000 WHERE account_id = " + testAccountId);

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Failed to reset test account balance");
        }
    }

    @Test
    public void testDeposit() {
        bankService.deposit(testAccountId, 500); // start 1000 → +500
        double balance = getBalance(testAccountId);
        assertEquals(1500.0, balance, 0.01, "Balance should be 1500 after deposit");
    }

    @Test
    public void testWithdraw() {
        // Ensure the account has 1500 before withdrawing
        bankService.deposit(testAccountId, 500); // start 1000 → +500 → 1500
        bankService.withdraw(testAccountId, 200); // 1500 → -200
        double balance = getBalance(testAccountId);
        assertEquals(1300.0, balance, 0.01, "Balance should be 1300 after withdrawal");
    }

    // Helper to check DB balance
    public double getBalance(int accountId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/banking_system",
                "root",
                "Welcome2025!");
             var stmt = conn.prepareStatement("SELECT balance FROM accounts WHERE account_id=?")) {
            stmt.setInt(1, accountId);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


}

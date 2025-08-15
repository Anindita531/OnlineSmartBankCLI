package com.bank;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankService bankService = new BankService();
        AdminService adminService = new AdminService();
        CustomerService customerService = new CustomerService();

        while (true) {
            System.out.println("\n--- Welcome to Secure Online Banking ---");
            System.out.println("1. Login as Admin");
            System.out.println("2. Login as Customer");
            System.out.println("3. Request New Account");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter username: ");
                    String uname = sc.nextLine();
                    System.out.print("Enter password: ");
                    String upass = sc.nextLine();
                    if (adminService.adminLogin(uname, upass)) adminMenu(adminService, sc);
                    break;
                case 2:
                    System.out.print("Enter account ID: ");
                    int aid = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter password: ");
                    String apass = sc.nextLine();
                    if (bankService.login(aid, apass)) customerMenu(bankService, aid, sc);
                    break;
                case 3:
                    // Request New Account
                    customerService.requestNewAccount();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option! Try again.");
            }
        }
    }

    private static void adminMenu(AdminService adminService, Scanner sc) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Pending Requests");
            System.out.println("2. Approve Request");
            System.out.println("3. Reject Request");
            System.out.println("4. View Transactions of an Account");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    adminService.viewAccountRequests();
                    break;
                case 2:
                    System.out.print("Enter Request ID to approve: ");
                    int rid = sc.nextInt();
                    sc.nextLine();
                    adminService.approveAccount(rid);
                    break;
                case 3:
                    System.out.print("Enter Request ID to reject: ");
                    rid = sc.nextInt();
                    sc.nextLine();
                    adminService.rejectAccount(rid);
                    break;
                case 4:
                    System.out.print("Enter account ID to view transactions: ");
                    int accId = sc.nextInt();
                    sc.nextLine();
                    adminService.viewAccountTransactions(accId);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option! Try again.");
            }
        }
    }

    private static void customerMenu(BankService bankService, int accountId, Scanner sc) {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Logout");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    bankService.viewBalance(accountId);
                    break;
                case 2:
                    System.out.print("Enter amount to deposit: ");
                    double dep = sc.nextDouble();
                    sc.nextLine();
                    bankService.deposit(accountId, dep);
                    break;
                case 3:
                    System.out.print("Enter amount to withdraw: ");
                    double w = sc.nextDouble();
                    sc.nextLine();
                    bankService.withdraw(accountId, w);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option! Try again.");
            }
        }
    }
}

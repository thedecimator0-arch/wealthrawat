package com.bank;

import com.bank.service.BankService;
import com.bank.util.DatabaseInitializer;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Entry point for the Apex Banking System CLI and local web dashboard.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        if (args.length > 0 && args[0].equals("--web")) {
            DatabaseInitializer.initialize();
            int port = Integer.getInteger("bank.http.port", 8000);
            new WebServer(Path.of(System.getProperty("bank.public.dir", "public"))).start(port);
            return;
        }

        System.out.println("=================================================================");
        System.out.println("          APEX BANKING SYSTEM - EXECUTIVE BACKEND ENGINE         ");
        System.out.println("=================================================================");

        // 1. Initialize DB Tables, Views, and Triggers
        DatabaseInitializer.initialize();

        BankService bankService = new BankService();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n-----------------------------------------------------------------");
                System.out.println("                    MAIN NAVIGATION MENU                         ");
                System.out.println("-----------------------------------------------------------------");
                System.out.println(" 1. View Customer Accounts (Query SQL View)");
                System.out.println(" 2. Deposit Money (Fires SQL Trigger)");
                System.out.println(" 3. Transfer Funds (ACID Transaction + SQL Trigger)");
                System.out.println(" 4. View Recent Transactions Log");
                System.out.println(" 5. Inspect Active Views and Triggers");
                System.out.println(" 6. Exit Application");
                System.out.print("\nSelect choice (1-6): ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid entry. Please enter a valid number.");
                    scanner.nextLine();
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                try {
                    switch (choice) {
                        case 1:
                            bankService.displayAccountsFromView();
                            break;
                        case 2:
                            System.out.print("Enter Account Number (e.g. ACC-1001): ");
                            String accNum = scanner.nextLine().trim();
                            System.out.print("Enter Deposit Amount in INR (₹): ");
                            double amt = scanner.nextDouble();
                            bankService.deposit(accNum, amt);
                            break;
                        case 3:
                            System.out.print("Enter Sender Account (e.g. ACC-1001): ");
                            String sender = scanner.nextLine().trim();
                            System.out.print("Enter Recipient Account (e.g. ACC-1002): ");
                            String recipient = scanner.nextLine().trim();
                            System.out.print("Enter Transfer Amount in INR (₹): ");
                            double tAmt = scanner.nextDouble();
                            bankService.transfer(sender, recipient, tAmt);
                            break;
                        case 4:
                            bankService.displayTransactions();
                            break;
                        case 5:
                            bankService.inspectMetadata();
                            break;
                        case 6:
                            System.out.println("[SYSTEM] Exiting Apex Banking Engine. All records saved.");
                            return;
                        default:
                            System.out.println("Invalid selection. Try again.");
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Operation failed: " + e.getMessage());
                }
            }
        }
    }
}

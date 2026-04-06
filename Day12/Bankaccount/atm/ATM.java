package Day12.Bankaccount.atm;

import Day12.Bankaccount.atm.exceptions.InsufficientFundsException;
import Day12.Bankaccount.atm.exceptions.InvalidPinException;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ATM {
    private Map<String, User> userDatabase;
    private User currentUser;
    private Account currentAccount;

    public ATM() {
        userDatabase = new HashMap<>();
    }

    public void registerUser(User user) {
        userDatabase.put(user.getUserId(), user);
    }

    public void authenticate(String userId, String pin) throws InvalidPinException {
        User user = userDatabase.get(userId);
        if (user == null || !user.validatePin(pin)) {
            throw new InvalidPinException("Invalid User ID or PIN. Please try again.");
        }
        this.currentUser = user;
        System.out.println("Authentication successful. Welcome, " + user.getName() + "!");
    }

    public void selectAccount(String accountNumber) {
        if (currentUser == null)
            return;
        Account acc = currentUser.getAccount(accountNumber);
        if (acc != null) {
            this.currentAccount = acc;
            System.out.println("Account " + accountNumber + " selected.");
        } else {
            System.out.println("Account not found.");
        }
    }

    public void checkBalance() {
        if (currentAccount != null) {
            System.out.printf("Current Balance: $%.2f%n", currentAccount.getBalance());
        } else {
            System.out.println("No account selected.");
        }
    }

    public void deposit(double amount) {
        if (currentAccount != null) {
            try {
                currentAccount.deposit(amount);
                System.out.printf("Deposited $%.2f successfully. New Balance: $%.2f%n", amount,
                        currentAccount.getBalance());
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    public void withdraw(double amount) {
        if (currentAccount != null) {
            try {
                currentAccount.withdraw(amount);
                System.out.printf("Withdrawn $%.2f successfully. New Balance: $%.2f%n", amount,
                        currentAccount.getBalance());
            } catch (InsufficientFundsException | IllegalArgumentException e) {
                System.err.println("Transaction Failed: " + e.getMessage());
            }
        }
    }

    public void printMiniStatement() {
        if (currentAccount != null) {
            System.out.println("--- Mini Statement ---");
            for (Transaction t : currentAccount.getTransactionHistory()) {
                System.out.println(t);
            }
            System.out.println("----------------------");
        }
    }

    public void logout() {
        this.currentUser = null;
        this.currentAccount = null;
        System.out.println("Logged out successfully.");
    }
}

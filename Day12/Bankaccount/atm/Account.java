package Day12.Bankaccount.atm;

import Day12.Bankaccount.atm.exceptions.InsufficientFundsException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Abstraction: Account is an abstract class defining common properties and methods
public abstract class Account {
    // Encapsulation: fields are protected/private to restrict direct access
    protected String accountNumber;
    protected double balance;
    protected List<Transaction> transactionHistory;

    public Account(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        if (initialBalance > 0) {
            recordTransaction("Deposit (Initial)", initialBalance);
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
        recordTransaction("Deposit", amount);
    }

    // Abstract method to be overridden by subclasses (Polymorphism)
    public abstract void withdraw(double amount) throws InsufficientFundsException;

    protected void recordTransaction(String type, double amount) {
        String txId = UUID.randomUUID().toString().substring(0, 8);
        transactionHistory.add(new Transaction(txId, type, amount));
    }
}

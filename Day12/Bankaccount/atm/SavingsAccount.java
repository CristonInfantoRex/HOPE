package Day12.Bankaccount.atm;

import Day12.Bankaccount.atm.exceptions.InsufficientFundsException;

// Inheritance: SavingsAccount IS-A Account
public class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500.0;
    private double interestRate;

    public SavingsAccount(String accountNumber, double initialBalance, double interestRate) {
        super(accountNumber, initialBalance);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    // Polymorphism: Specific implementation for SavingsAccount withdrawal
    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (balance - amount < MIN_BALANCE) {
            throw new InsufficientFundsException(
                    "Cannot withdraw. Minimum balance of $" + MIN_BALANCE + " must be maintained.");
        }
        balance -= amount;
        recordTransaction("Withdrawal", amount);
    }
}

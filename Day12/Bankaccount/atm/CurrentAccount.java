package Day12.Bankaccount.atm;

import Day12.Bankaccount.atm.exceptions.InsufficientFundsException;

// Inheritance: CurrentAccount IS-A Account
public class CurrentAccount extends Account {
    private double overdraftLimit;

    public CurrentAccount(String accountNumber, double initialBalance, double overdraftLimit) {
        super(accountNumber, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    // Polymorphism: Specific implementation for CurrentAccount withdrawal allowing
    // overdraft
    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (balance + overdraftLimit < amount) {
            throw new InsufficientFundsException("Cannot withdraw. Overdraft limit exceeded.");
        }
        balance -= amount;
        recordTransaction("Withdrawal (Current)", amount);
    }
}

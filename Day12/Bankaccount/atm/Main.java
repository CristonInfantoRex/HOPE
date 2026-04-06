package Day12.Bankaccount.atm;

import Day12.Bankaccount.atm.exceptions.InvalidPinException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing ATM Machine Simulation...");

        // Setup mock data
        ATM atm = new ATM();

        User alice = new User("U1001", "Alice Smith", "1234");
        // Savings Account with minimum balance required
        SavingsAccount aliceSavings = new SavingsAccount("SA-1001", 1000.0, 3.5);
        // Current Account with an overdraft limit
        CurrentAccount aliceCurrent = new CurrentAccount("CA-1002", 200.0, 500.0);

        alice.addAccount(aliceSavings);
        alice.addAccount(aliceCurrent);

        atm.registerUser(alice);

        // Run test behaviors
        try {
            System.out.println("\n--- Test 1: Invalid Authentication ---");
            atm.authenticate("U1001", "wrongpin");
        } catch (InvalidPinException e) {
            System.err.println(e.getMessage());
        }

        try {
            System.out.println("\n--- Test 2: Valid Authentication & Savings Operations ---");
            atm.authenticate("U1001", "1234");

            System.out.println("\nSelect Savings Account:");
            atm.selectAccount("SA-1001");
            atm.checkBalance(); // Should be 1000

            System.out.println("\nAttempt to withdraw $600 (leaves $400, below $500 min):");
            atm.withdraw(600.0); // Should fail due to min balance

            System.out.println("\nAttempt to withdraw $300:");
            atm.withdraw(300.0); // Should succeed, leaves $700

            atm.deposit(150.0);

            atm.printMiniStatement();

            System.out.println("\n--- Test 3: Polymorphism & Current Account Operations ---");
            System.out.println("\nSelect Current Account:");
            atm.selectAccount("CA-1002");
            atm.checkBalance(); // Should be $200

            System.out.println("\nAttempt to withdraw $400 (exceeds balance but within overdraft):");
            atm.withdraw(400.0); // Should succeed, balance becomes -$200

            atm.checkBalance();

            System.out.println("\nAttempt to withdraw $400 again (exceeds overdraft of 500 total):");
            atm.withdraw(400.0); // balance is -200, withdraw 400 means -600 (limit is 500) -> fails

            atm.printMiniStatement();

            System.out.println("\n--- Logout ---");
            atm.logout();

        } catch (InvalidPinException e) {
            System.err.println(e.getMessage());
        }
    }
}

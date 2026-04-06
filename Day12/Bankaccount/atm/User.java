package Day12.Bankaccount.atm;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String pinHash; // Simple representation of an encrypted PIN
    private List<Account> accounts;

    public User(String userId, String name, String pin) {
        this.userId = userId;
        this.name = name;
        this.pinHash = hashPin(pin);
        this.accounts = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(accountNumber))
                return acc;
        }
        return null;
    }

    public boolean validatePin(String inputPin) {
        return this.pinHash.equals(hashPin(inputPin));
    }

    // Abstraction: Hiding the complexity of pin hashing (even if it's simple here)
    private String hashPin(String pin) {
        // In a real system, this would use a cryptographic hash like BCrypt
        return "HASH_" + pin;
    }
}

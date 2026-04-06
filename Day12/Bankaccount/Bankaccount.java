package Day12.Bankaccount;
import java.util.*;

// ABSTRACT CLASS (Abstraction + Encapsulation)
import java.util.*;

// ABSTRACT CLASS
abstract class BankAccount {
    private String accountNumber;
    private double balance;
    private int pin;

    public BankAccount(String accountNumber, double balance, int pin) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.pin = pin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    protected double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean validatePin(int inputPin) {
        return this.pin == inputPin;
    }

    public abstract void deposit(double amount);
    public abstract void withdraw(double amount);
}

// INHERITANCE
class SavingsAccount extends BankAccount {

    private static final double MIN_BALANCE = 500;

    public SavingsAccount(String accNo, double balance, int pin) {
        super(accNo, balance, pin);
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }
        setBalance(getBalance() + amount);
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }

        if (getBalance() - amount < MIN_BALANCE) {
            throw new IllegalStateException("Minimum balance violation");
        }

        setBalance(getBalance() - amount);
    }
}

// TRANSACTION CLASS
class Transaction {
    private String type;
    private double amount;
    private Date date;

    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.date = new Date();
    }

    public void printTransaction() {
        System.out.println(type + " | Amount: " + amount + " | Date: " + date);
    }
}

// SERVICE LAYER
class ATMService {

    private BankAccount account;
    private List<Transaction> transactions = new ArrayList<>();

    public ATMService(BankAccount account) {
        this.account = account;
    }

    public void deposit(double amount) {
        account.deposit(amount);
        transactions.add(new Transaction("Deposit", amount));
    }

    public void withdraw(double amount) {
        account.withdraw(amount);
        transactions.add(new Transaction("Withdraw", amount));
    }

    public void checkBalance() {
        System.out.println("Balance: " + account.getBalance());
    }

    public void printMiniStatement() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : transactions) {
            t.printTransaction();
        }
    }
}

// ATM CLASS
class ATM {

    private ATMService service;

    public ATM(ATMService service) {
        this.service = service;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1.Deposit 2.Withdraw 3.Check Balance 4.Statement 5.Exit");
            int choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter amount: ");
                        service.deposit(sc.nextDouble());
                        break;

                    case 2:
                        System.out.print("Enter amount: ");
                        service.withdraw(sc.nextDouble());
                        break;

                    case 3:
                        service.checkBalance();
                        break;

                    case 4:
                        service.printMiniStatement();
                        break;

                    case 5:
                        System.out.println("Thank you!");
                        return;

                    default:
                        System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

// MAIN CLASS (RENAMED)
public class Bankaccount {
    public static void main(String[] args) {

        BankAccount acc = new SavingsAccount("ACC123", 5000, 1234);

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter PIN: ");
        int enteredPin = sc.nextInt();

        if (!acc.validatePin(enteredPin)) {
            System.out.println("Invalid PIN. Access denied.");
            return;
        }

        ATMService service = new ATMService(acc);
        ATM atm = new ATM(service);

        atm.start();
    }
}
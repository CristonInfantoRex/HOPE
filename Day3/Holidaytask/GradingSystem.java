package Day3.Holidaytask;
import java.util.*;
public class GradingSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the mark (0-100): ");
        int mark = scanner.nextInt();
        char grade;

        switch (mark / 10) {
            case 10:
                grade = 'S';
            case 9:
                grade = 'A';
                break;
            case 8:
                grade = 'B';
                break;
            case 7:
                grade = 'C';
                break;
            case 6:
                grade = 'D';
                break;
            default:
                grade = 'F';
                break;
        }

        System.out.println("Grade: " + grade);
        scanner.close();
    }
}

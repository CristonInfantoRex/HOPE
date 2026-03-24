package Day3.Holidaytask;
import java.util.*;
public class LC2525 {
    public static void main(String[] args) {
        Scanner input_reader = new Scanner(System.in);

        int length = input_reader.nextInt();
        int width = input_reader.nextInt();
        int height = input_reader.nextInt();
        int mass = input_reader.nextInt();

        long volume = (long) length * width * height;
        boolean is_bulky = (length >= 10000 || width >= 10000 || height >= 10000 || volume >= 1000000000);
        boolean is_heavy = (mass >= 100);

        String category;
        if (is_bulky && is_heavy) {
            category = "Both";
        } else if (!is_bulky && !is_heavy) {
            category = "Neither";
        } else if (is_heavy) {
            category = "Heavy";
        } else {
            category = "Bulky";
        }

        System.out.println(category);
        input_reader.close();
    }
}

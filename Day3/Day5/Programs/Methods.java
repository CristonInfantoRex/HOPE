package Day5.Programs;
import java.util.*;
public class Methods {
    public static int square(int n) {
        return n * n;
    }

    public int square2(int n) {
        return n * n;
    }

    public static void main(String[] args) {
        //calling static method
        System.out.println(square(25));

        //calling non static method
        var sqr = new Methods();
        System.out.println(sqr.square2(25));
    }
}

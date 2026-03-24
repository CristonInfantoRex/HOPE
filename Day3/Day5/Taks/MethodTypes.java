package Day5.Taks;
import java.util.*;
public class MethodTypes {
    String msg;

    // non parameterized constructor
    MethodTypes() {
        msg = "Hi";
    }

    // parameterized constructor
    MethodTypes(String s) {
        msg = s;
    }

    // non parameterized instance method
    void show() {
        System.out.println(msg);
    }

    // parameterized instance method
    void set(String s) {
        msg = s;
    }

    // non parameterized static method
    static void log() {
        System.out.println("Static");
    }

    // parameterized static method
    static int sq(int n) {
        return n * n;
    }

    public static void main(String[] args) {
        log();
        System.out.println(sq(5));

        MethodTypes f1 = new MethodTypes();
        f1.show();

        MethodTypes f2 = new MethodTypes("Java");
        f2.set("Updated");
        f2.show();
    }
}

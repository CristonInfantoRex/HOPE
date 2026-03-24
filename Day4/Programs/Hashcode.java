package Day4.Programs;
import java.util.*;
public class Hashcode {
    public static void main(String[] args) {
        int[] arr1 = { 1, 2, 3 };
        Integer[] arr2 = { 1, 2, 3 };
        int var1 = 1;
        Integer var2 = 1;

        System.out.println("int[0] " + arr1.hashCode());
        System.out.println("Integer[0] " + arr2[0].hashCode());
        System.out.println("Integer " + var2.hashCode());
    }
}

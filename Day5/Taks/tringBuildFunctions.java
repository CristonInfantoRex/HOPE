package Day5.Taks;
import java.util.*;
public class tringBuildFunctions {
    public static void main(String[] args) {
        StringBuilder stringHandler = new StringBuilder("Hello");

        stringHandler.append(" world");
        System.out.println(stringHandler);

        stringHandler.insert(5, " yesterday");
        System.out.println(stringHandler);

        stringHandler.replace(6, 9, "celesteela");
        System.out.println(stringHandler);

        stringHandler.delete(6, 15);
        System.out.println(stringHandler);

        stringHandler.reverse();
        System.out.println(stringHandler);

        stringHandler.reverse();
        System.out.println(stringHandler.length());
        System.out.println(stringHandler.charAt(1));

        stringHandler.setCharAt(0, 'h');
        System.out.println(stringHandler);
    }
}

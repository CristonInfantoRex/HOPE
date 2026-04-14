package Day13.Programs;
import java.util.*;
public class SetImplementtion {
    public static void main(String[] args){
        Set<Integer> set = new HashSet<>();
        Integer a = 1;

        set.add(a);
        set.add(33);
        set.add(23);
        IO.println(a.hashCode());
        IO.println(set);
        set.add(2);
        IO.println(set.contains(1));

        Set<String> set2 = new HashSet<>();
        String b = "hello";

        set2.add(b);
        set2.add("name");
        set2.add("name3");
        IO.println(b.hashCode());
        IO.println(set2);
        set2.add("yes");
        IO.println(set2.contains("hello"));
    }
}

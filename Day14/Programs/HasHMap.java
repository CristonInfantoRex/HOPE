package Day14.Programs;
import java.util.*;
public class HasHMap {
    public static void main(String[] args) {
        HashMap<String, Integer> map = new HashMap<>();
        System.out.println(map.get("hello"));
        System.out.println(map.getOrDefault("name1", 8));
        map.put("name2", 10);

        for (String s : map.keySet()) {
            IO.println(s + " " + map.get(s));
        }
    }
}

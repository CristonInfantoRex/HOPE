package Day14.Programs;
import java.util.*;
public class LinkedHashmapImp {
    void main()
    {
        var map = new LinkedHashMap<Integer, String>();
        map.put(3, "Three");
        map.put(1, "One");
        map.put(2, "Two");

        for(int i: map.keySet()){
            IO.println(i + " " + map.get(i));
    }
    }
}

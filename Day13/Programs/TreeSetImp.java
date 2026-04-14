package Day13.Programs;
import java.util.*;
public class TreeSetImp {
    public static void main(String [] args)
    {
        var set = new TreeSet<Integer>();

        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        set.add(4);

        IO.println(set);
    }

}

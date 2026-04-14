package Day17.Programs;
import java.util.*;
public class IteratorImplementation {
    public static void main(String [] args)
    {
        List<Integer> list  = new ArrayList<>();
        list.add(100);
        list.add(200);
        list.add(300);
        list.add(299);
        Iterator<Integer> iter = list.iterator();
        while(iter.hasNext())
        {
            int curr = iter.next();
            if(curr % 2 == 0)
            {
                iter.remove();
            }
            else
            {
                IO.println(curr);
            }
        }
    }
}

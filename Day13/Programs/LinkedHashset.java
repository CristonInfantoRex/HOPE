package Day13.Programs;
import java.util.*;
public class LinkedHashset {
    public static void main(String [] args)
    {
        var llset = new LinkedHashSet<String>();
        llset.add("str1");
        llset.add("str2");
        llset.add("Str3");
        llset.add("str4");
        llset.add("str1");
        llset.add("str2");
        llset.add("Str3");
        llset.add("str4");

        IO.println(llset);
    }
}

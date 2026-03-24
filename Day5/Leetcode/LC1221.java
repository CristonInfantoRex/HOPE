package Day5.Leetcode;
import java.util.*;
public class LC1221 {
    public int balancedStringSplit(String s) {
        int count = 0;
        int subcount = 0;

        for (char ch : s.toCharArray()) {
            if (ch == 'R') count++;
            else count--;

            if (count == 0) subcount++;
        }

        return subcount;
    }
}

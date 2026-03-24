package Day5.Leetcode;
import java.util.*;
public class LC58 {
    public int lengthOfLastWord(String s) {
        var spaceStr = s.strip();
        int end = spaceStr.length() - 1;
        int count = 0;

        while (end >= 0) {
            if (spaceStr.charAt(end) == ' ') return count;

            count++;
            end--;
        }

        return count;
    }
}

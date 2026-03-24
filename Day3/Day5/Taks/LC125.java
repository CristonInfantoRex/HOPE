package Day5.Taks;
import java.util.*;
public class LC125 {
    public boolean isPalindrome(String s) {
        String cs = s.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

        int left = 0;
        int right = cs.length() - 1;

        while (left < right) {
            if (cs.charAt(left) == cs.charAt(right)) {
                left++;
                right--;
            } else {
                return false;
            }
        }

        return true;
    }
}

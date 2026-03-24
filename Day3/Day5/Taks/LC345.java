package Day5.Taks;
import java.util.*;
public class LC345 {
    public String reverseVowels(String s) {
        var chars = new ArrayList<Character>();
        var index = new ArrayList<Integer>();

        for (int i = 0; i < s.length(); i++) {
            char lower = Character.toLowerCase(s.charAt(i));
            if (
                    lower == 'a' ||
                            lower == 'e' ||
                            lower == 'i' ||
                            lower == 'o' ||
                            lower == 'u'
            ) {
                chars.add(s.charAt(i));
                index.add(i);
            }
        }

        Collections.reverse(index);
        var sb = new StringBuilder(s);

        for (int i = 0; i < index.size(); i++) {
            sb.setCharAt(index.get(i), chars.get(i));
        }

        return sb.toString();
    }
}

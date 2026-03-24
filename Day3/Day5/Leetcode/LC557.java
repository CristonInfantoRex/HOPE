package Day5.Leetcode;
import java.util.*;
public class LC557 {
    public String reverseWords(String s) {
        String[] sent = s.split(" ");
        var finalSb = new StringBuilder();

        for (int i = 0; i < sent.length; i++) {
            var sb = new StringBuilder(sent[i]);
            finalSb.append(sb.reverse().toString());
            finalSb.append(" ");
        }

        return new String(finalSb.toString().strip());
    }
}

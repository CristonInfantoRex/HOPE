package Day15.Programs;
import java.util.*;
public class Leetcode169 {
    public int majorityElement(int[] nums) {
        int comp = nums.length / 2;
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i : nums) {
            map.put(i, map.getOrDefault(i, 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int key = entry.getKey();
            int val = entry.getValue();

            if (val > comp) {
                return key;
            }
        }

        return 0;
    }
}

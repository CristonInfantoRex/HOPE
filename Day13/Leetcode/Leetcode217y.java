package Day13.Leetcode;
import java.util.*;
public class Leetcode217y {
    public boolean containsDuplicate(int[] nums) {
        var set = new HashSet<Integer>();

        for(int i: nums){
            if(!set.add(i)) return true;
        }

        return false;
    }
}

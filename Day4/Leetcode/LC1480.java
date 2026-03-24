package Day4.Leetcode;
import java.util.*;
public class LC1480 {
    public int[] runningSum(int[] nums) {
        int curr = nums[0];
        for(int i = 1; i < nums.length; i++){
            curr += nums[i];
            nums[i] = curr;
        }

        return nums;
    }
}

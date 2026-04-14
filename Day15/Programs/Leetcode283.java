package Day15.Programs;
import java.util.*;
public class Leetcode283 {
    public void moveZeroes(int[] nums) {
        int last_non_zero = 0;

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {

                int temp = nums[last_non_zero];
                nums[last_non_zero] = nums[i];
                nums[i] = temp;

                last_non_zero++;
            }
        }
    }
}

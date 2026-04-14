package Day16.Leetcode;
import java.util.*;
public class Leetocde1518 {
    public int numWaterBottles(int numBottles, int numExchange) {
        int emptyBottles = numBottles;
        int totalDrank = numBottles;

        while (emptyBottles >= numExchange) {
            int newFull = emptyBottles / numExchange;
            totalDrank += newFull;
            emptyBottles = newFull + (emptyBottles % numExchange);
        }

        return totalDrank;
    }
}

package Day3.Holidaytask;

public class LC2864 {
    public String maximumOddBinaryNumber(String s) {
        int n = s.length();
        int onesCount = 0;

        for (char c : s.toCharArray()) {
            if (c == '1') {
                onesCount++;
            }
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < onesCount - 1; i++) {
            result.append('1');
        }

        int zerosCount = n - onesCount;
        for (int i = 0; i < zerosCount; i++) {
            result.append('0');
        }

        result.append('1');

        return result.toString();
    }
}

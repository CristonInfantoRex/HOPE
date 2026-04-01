package Day11.Programs;
import java.util.*;
public class WrapperClass2 {
    public static void main(String [] args)
    {
        int a = 10;
        IO.println(a);
        Integer objA = Integer.valueOf(a);
        /*Integer.valueOf is used to convert primitive to  objectvalue*/
        System.out.println(objA);
        System.out.println(a == objA);/*Inga sout la  == podum bothu apdiye athu normal integer
        a change aguthu using 'objA.intValue'()*/
        System.out.println();
        System.out.println(objA.hashCode());

    }
}

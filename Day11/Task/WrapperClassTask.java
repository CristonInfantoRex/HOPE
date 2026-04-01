package Day11.Task;
import java.util.*;
public class WrapperClassTask {
    public static void main(String[] args) {
        int a = 10;
        char b = 'a';
        double c = 10.4;
        float d = 20.33f;
        boolean bool = false;

        Integer integer = Integer.valueOf(a);
        System.out.println(integer);
        System.out.println(integer.getClass().getSimpleName());

        Character charac = Character.valueOf(b);
        System.out.println(charac);
        System.out.println(charac.getClass().getSimpleName());

        Double dou = Double.valueOf(c);
        System.out.println(dou);
        System.out.println(dou.getClass().getSimpleName());

        Float fl = Float.valueOf(d);
        System.out.println(fl);
        System.out.println(fl.getClass().getSimpleName());

        Boolean boo = Boolean.valueOf(bool);
        System.out.println(boo);
        System.out.println(boo.getClass().getSimpleName());

        int aa = integer.intValue();
        System.out.println(aa);
        System.out.println(((Object)aa).getClass().getSimpleName());/*To get typeof in primitive type we
        convert it to object inside itelf and then find it*/
        char bb = charac.charValue();
        double cc = dou.doubleValue();
        boolean boool = boo.booleanValue();


        String str = "123";
        int hi = Integer.parseInt(str);
        System.out.println(((Object)hi).getClass().getSimpleName());

        String inte = integer.toString();//this is for objectType
        String in = Integer.toString(a);//This is for primitiveType
        String ch = Character.toString(b);


    }
}
package Day11.Programs;

public class AutoBoxing {
    public static void main(String [] g)
    {
        Integer a = 100;
        Integer b = 100;
        System.out.println(a == b);//This checks the value and the reference ***
        System.out.println(a.equals(b));//This checks the values only

        Float af = 100.0f;
        Float bf = 100.0f;
        System.out.println(a == b);//This checks the value and the reference ***
        System.out.println(a.equals(b));//This checks the values only

        /*why there is a difference between the answers in Float and Integer check that*/
    }

}

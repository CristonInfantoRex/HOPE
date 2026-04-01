package Day11.Programs.Packages.p2;
import Day11.Programs.Packages.p1.Packagep1;
public class Packagep2
{
    public static void main(String [] args)
    {
        B b = new B();
        b.display(12);
    }
}
class B extends Packagep1
{
     public void diplay(int value)
     {
         super.display(value);
     }
}

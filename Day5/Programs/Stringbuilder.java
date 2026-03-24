package Day5.Programs;
import java.lang.StringBuffer;

public class Stringbuilder {



        public static void main(String[] args) {
            var sb = new StringBuilder("jill");
            //sb.append(" chris");
            //sb.reverse();
            System.out.println(sb.toString());

            var sbr = new StringBuffer("Wesker ");
            sbr.append(" yes");
            sbr.reverse();
            System.out.println(sbr.toString());
        }
}

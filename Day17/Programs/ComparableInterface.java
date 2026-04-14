package Day17.Programs;
import java.util.*;
public class ComparableInterface {
    public static void main(String [] args)
    {
        List<Student> list = new ArrayList<>();
        list.add(new Student("Babu",1));
        list.add(new Student("Abel",2));
        list.add(new Student("Moorthi",3));
        Collections.sort(list);
        for(Student s : list)
        {
            IO.println(s.rollno);
        }
    }
}
class Student implements Comparable<Student>
{
    String name;
    int rollno;
    Student(String name,int rollno)
    {
        this.rollno = rollno;
        this.name = name;
    }
    public int compareTo(Student s)
    {
        return this.rollno - s.rollno;
    }
}

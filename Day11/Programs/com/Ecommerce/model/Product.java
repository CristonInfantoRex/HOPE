package Day11.Programs.com.Ecommerce.model;
public class Product {
    private int id;
    private  String name;
    private int price;
    public Product(int id,String name,int price)
    {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    public int getPrice()
    {
        return this.price;
    }
    public void display()
    {
        System.out.println("The id is: "+this.id);
        System.out.println("The name is: "+this.name);
        System.out.println("The price is: "+this.price);
    }
}

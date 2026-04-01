package Day11.Programs.com.Ecommerce.service;
import Day11.Programs.com.Ecommerce.model.Product;
import java.util.*;
public class CartService {
    private ArrayList<Product> cart = new ArrayList<>();
    public void addProduct(Product p)
    {
        cart.add(p);
    }
    public int calculateTotal()
    {
        int total = 0;
        for(Product p : cart)
        {
            total += p.getPrice();
        }
        return total;
    }
    public void showCart()
    {
        for(Product p : cart)
        {
            p.display();
        }
    }
}

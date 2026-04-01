package Day11.Programs.com.Ecommerce.main;
import Day11.Programs.com.Ecommerce.service.CartService;
import Day11.Programs.com.Ecommerce.model.Product;
import Day11.Programs.com.Ecommerce.util.ProductUtil;

public class MainApp {
    public static void main(String [] args)
    {
        CartService cart = new CartService();
        Product p1 = new Product(1, "Samsung A56", ProductUtil.applyDiscount(55000));
        Product p2 = new Product(2,"Iphone 16 Plus",74000);
        cart.addProduct(p1);
        cart.addProduct(p2);
        cart.showCart();
        int totalAmount = cart.calculateTotal();
        System.out.println("The Total Amount is: "+totalAmount);

    }
}

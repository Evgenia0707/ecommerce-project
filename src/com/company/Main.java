package com.company;

import com.company.balance.Balance;
import com.company.balance.CustomerBalance;
import com.company.balance.GiftCardBalance;
import com.company.category.Category;
import com.company.discount.Discount;
import com.company.order.Order;
import com.company.order.OrderService;
import com.company.order.OrderServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

//create dataBase
        DataGenerator.createCustomer();//create()for customer
        DataGenerator.createCategory();
        DataGenerator.createProduct();
        DataGenerator.createBalance();
        DataGenerator.createDiscount();

        Scanner scanner = new Scanner(System.in);
//pick 1 user
        System.out.println("Select Customer: ");

        for (int i = 0; i < StaticConstants.CUSTOMER_LIST.size(); i++) {//create / put in DataBase
            System.out.println("Type " + i + " for customer: " + StaticConstants.CUSTOMER_LIST.get(i).getUserName());
        }

        Customer customer = StaticConstants.CUSTOMER_LIST.get(scanner.nextInt());

        Cart cart = new Cart(customer);// create empty cart for customer

        while (true) {
            System.out.println("What would you like to do? Just type id selection");

            for (int i = 0; i < prepareMenuOptions().length; i++) {//choose what to do
                System.out.println(i + "-" + prepareMenuOptions()[i]);
            }
            int menuSelection = scanner.nextInt();

            switch (menuSelection) {

                case 0://list categories
                    for (Category category : StaticConstants.CATEGORY_LIST) {
                        System.out.println("Category Code: " + category.generateCategoryCode() + " category name: " + category.getNames());
                    }
                    break;

                case 1://list products
                    try {
                        for (Product product : StaticConstants.PRODUCT_LIST) {
                            System.out.println("Product Name: " + product.getName() + " Product Category Name:" + product.getCategoryName());
                        }
                    } catch (Exception e) {
                        System.out.println("Product could not printed because category not found for product name: " + e.getMessage().split(",")[1]);
                    }
                    break;

                case 2://list discount
                    for (Discount discount : StaticConstants.DISCOUNT_LIST) {
                        System.out.println("Discount name: " + discount.getName() + " discount threshold amount: " + discount.getThresholdAmount());
                    }
                    break;
                case 3://see balance
                    CustomerBalance cBalance = findCustomerBalance(customer.getId());
                    GiftCardBalance gBalance = findGiftCardBalance(customer.getId());
                    double totalBalance = cBalance.getBalance() + gBalance.getBalance();
                    System.out.println("Total Balance: " + totalBalance);
                    System.out.println("Customer Balance: " + cBalance.getBalance());
                    System.out.println("Gift Card Balance: " + gBalance.getBalance());

                    break;
                case 4://Add Balance
                    CustomerBalance customerBalance = findCustomerBalance(customer.getId());
                    GiftCardBalance giftCardBalance = findGiftCardBalance(customer.getId());

                    System.out.println("Which Account would you like to add?");
                    System.out.println("Type 1 for Customer Balance: " + customerBalance.getBalance());
                    System.out.println("Type 2 for Gift Card Balance: " + giftCardBalance.getBalance());

                    int balanceAccountSelection = scanner.nextInt();

                    System.out.println("How much you would like to add?");
                    double additionalAmount = scanner.nextInt();

                    switch (balanceAccountSelection) {

                        case 1:
                            customerBalance.addBalance(additionalAmount);
                            System.out.println("New Customer Balance: " + customerBalance.getBalance());
                            break;
                        case 2:
                            giftCardBalance.addBalance(additionalAmount);
                            System.out.println("New Gift Card Balance: " + giftCardBalance.getBalance());
                            break;
                    }
                    break;
                case 5://Place an order
//                    Map<Product, Integer> cartMap = new HashMap<>(); //TODO removed new map
//                    cart.setProductMap(cartMap);// set up cart //TODO initiated in class no need to set map here
                    while (true) {//to keep ask about products what need
                                                System.out.println("Which product you want to add to your cart. For exit product selection Type: exit");

                        for (Product product : StaticConstants.PRODUCT_LIST) {//show products - customer may choose

                            try {
                                System.out.println(
                                        "id: " + product.getId() + " price: " + product.getPrice() +
                                                " product category: " + product.getCategoryName() +
                                                " stock: " + product.getRemainingStock() +
                                                " product delivery due: " + product.getDeliveryDueDate());
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        //add product
                        String productId = scanner.next();

                        try {
                            Product product = findProductById(productId);//create() in Main/

                            if (! putItemToCartIfStockAvailable(cart, product)) {
                                System.out.println("Stock is insufficient. Please try again");
                                continue;// back to loop
                            }

                        } catch (Exception e) {
                            System.out.println("Product does not exist. Please try again");
                            continue;
                        }

                        System.out.println("Do you want to add more product? Type Y for adding more, N for exit");
                        String decision = scanner.next();
                        if (!decision.equalsIgnoreCase("Y")) {
                            break;
                        }
                    }
                    System.out.println("Seem there are discount options. Do you want to see and apply to your cart if it is applicable. For no discount type \"N\"");
                    for (Discount discount : StaticConstants.DISCOUNT_LIST) {
                        System.out.println("Discount Id " + discount.getId() + "discount name: " + discount.getName());
                    }

                    String discountId = scanner.next();
                    if (!discountId.equalsIgnoreCase("N")) {
                        try {
                            Discount discount = findDiscountById(discountId);

                            if (  discount.decideDiscountIsApplicableToCart(cart)) {// TODO ( ! disc) "sorry, discount is not available for the amount
                                cart.setDiscountId(discount.getId());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    OrderService orderService = new OrderServiceImpl();
                    String result = orderService.placeOrder(cart);
                    if (result.equals("Order has been placed successfully")) {
                        System.out.println("Order is successful");
                        updateProductStock(cart.getProductMap());
//                        cart.setProductMap(new HashMap<>()); //TODO removed map reset - keeping count ONE CART for ALL ORDERS
                        //empty shopping cart
                        //cart.getProductMap().clear();
                        cart.setDiscountId(null);
                    } else {
                        System.out.println(result);
                    }
                    break;

                case 6://See Cart
                    System.out.println("Your Cart");
                    if ( ! cart.getProductMap().keySet().isEmpty()){
                        for (Product product : cart.getProductMap().keySet()){
                            System.out.println("Product name: " + product.getName() + " count: " + cart.getProductMap().get(product));
                        }
                    }else {
                        System.out.println("Your cart is empty");
                    }

                    break;

                case 7://See order details

                    printOrdersByCustomerId(customer.getId());
                    break;

                case 8://See your address

                    printAddressByCustomerId(customer);
                    break;

                case 9://Close App

                    System.exit(1);
                    break;
            }
        }
    }

    private static void printAddressByCustomerId(Customer customer) {
        for (Address address : customer.getAddress()){
            System.out.println(" Street Name: " + address.getStreetName() +
                    " Street Number: " + address.getStreetNumber() + "ZipCode:  "
                    + address.getZipCode() + " State: " + address.getState());
        }
    }

    private static void printOrdersByCustomerId(UUID customerId) {
        for (Order order : StaticConstants.ORDER_LIST){
            if (order.getCustomerId().toString().equals(customerId.toString())){
                System.out.println("Order status: " + order.getOrderStatus() + " order amount " + order.getPaidAmount() +
                        " order date " +order.getOrderDate());
            }
        }
    }

    private static void updateProductStock(Map<Product, Integer> map) { //TODO not working (set stock -update)
        for (Product product : map.keySet()) {
            product.setRemainingStock(product.getRemainingStock() - map.get(product));
        }

   }

    private static Discount findDiscountById(String discountId) throws Exception {
        for (Discount discount : StaticConstants.DISCOUNT_LIST) {
            if (discount.getId().toString().equals(discountId)) {
                return discount;
            }
        }
        throw new Exception("Discount could not applied because could not found");
    }
    private static boolean putItemToCartIfStockAvailable(Cart cart, Product product) {//check stock, quantity

        System.out.println("Please provide product count: ");
        Scanner scanner = new Scanner(System.in);
        int count = scanner.nextInt();//quantity for buy

        Integer cartCount = cart.getProductMap().get(product);

        if (cartCount != null && product.getRemainingStock() > (cartCount + count)) {
            cart.getProductMap().put(product, cartCount + count);
            return true;
        } else if (product.getRemainingStock() >= count) {
            cart.getProductMap().put(product, count);
            return true;
        }
        return false;
    }

    private static Product findProductById(String productId) throws Exception {
        for (Product product : StaticConstants.PRODUCT_LIST) {
            if (product.getId().toString().equals(productId)) {
                return product;
            }
        }
        throw new Exception("Product not fund");
    }

    private static GiftCardBalance findGiftCardBalance(UUID customerId) {

        for (Balance giftCardBalance : StaticConstants.GIFT_CARD_BALANCE_LIST) {
            if (giftCardBalance.getCustomerId().toString().equals(customerId.toString())) {
                return (GiftCardBalance) giftCardBalance;
            }
        }
        GiftCardBalance giftCardBalance = new GiftCardBalance(customerId, 0d);
        StaticConstants.GIFT_CARD_BALANCE_LIST.add(giftCardBalance);

        return giftCardBalance;

    }

    private static CustomerBalance findCustomerBalance(UUID customerId) {

        for (Balance customerBalance : StaticConstants.CUSTOMER_BALANCE_LIST) {
            if (customerBalance.getCustomerId().toString().equals(customerId.toString())) {
                return (CustomerBalance) customerBalance;
            }
        }

        CustomerBalance customerBalance = new CustomerBalance(customerId, 0d);
        StaticConstants.CUSTOMER_BALANCE_LIST.add(customerBalance);

        return customerBalance;

    }

       private static String[] prepareMenuOptions() {
        return new String[]{"List Categories", "List Products", "List Discounts", "See Balance", "Add Balance",
                "Place an order", "See Cart", "See order details", "See your address", "Close App"};
    }


}
/*

   if(customerBalance.getCustomerId().toString().equals(customerId.toString())){
   ?toString?

In this case, we are comparing to UUID objects. If you don't use toString method for comparing, you will compare instance of objects. If object references are not same, even if UUID values same equals method can return false.
For example;
Product product1 = new Product(name);
Product product2 = new Product(name);
if you compare each objects like
if(product1.equals(product2))
this will return false because references are different
but if you compare
if(product1.getName().equals(product2.getName()))
it will return true if name values are same
with toString we want to compare value of UUID objects not different UUID object references.
Think about primitive types and wrapper classes
 */
package model;

import java.util.List;
import java.util.Date;

/**
 * ============================================================
 *  Order.java — Model Class (Represents a placed order)
 * ============================================================
 *
 *  OOP CONCEPT: COMPOSITION (again, stronger example)
 *  ---------------------------------------------------
 *  An Order contains:
 *    - A User object   (who placed the order)
 *    - A List of CartItem objects (what was ordered)
 *
 *  This models reality: an order belongs to someone and
 *  contains multiple items. Java classes can hold other objects!
 *
 *  OOP CONCEPT: ENCAPSULATION
 *  --------------------------
 *  Private fields, public getters/setters.
 *  The getTotalPrice() method is a calculated behaviour —
 *  it adds up all item subtotals automatically.
 * ============================================================
 */
public class Order {

    // -------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------
    private int            id;
    private User           user;           // Composition: Order HAS-A User
    private List<CartItem> items;          // Composition: Order HAS-A List of CartItems
    private double         totalPrice;
    private Date           orderDate;

    // -------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------
    public Order(int id, User user, List<CartItem> items, double totalPrice, Date orderDate) {
        this.id         = id;
        this.user       = user;
        this.items      = items;
        this.totalPrice = totalPrice;
        this.orderDate  = orderDate;
    }

    public Order() {}

    // -------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    // -------------------------------------------------------
    // COMPUTED BEHAVIOUR — calculates total from items list
    // This is useful if you want to re-calculate dynamically
    // -------------------------------------------------------
    public double calculateTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal(); // Uses CartItem's own method
        }
        return total;
    }

    // -------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", user=" + user.getFullName() +
               ", total=GH₵" + totalPrice + ", date=" + orderDate + "}";
    }
}

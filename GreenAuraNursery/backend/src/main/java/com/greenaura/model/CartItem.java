package com.greenaura.model;

/**
 * ============================================================
 *  CartItem.java — Model Class (Represents one item in a cart)
 * ============================================================
 *
 *  OOP CONCEPT: CLASS RELATIONSHIPS (Composition)
 *  -----------------------------------------------
 *  A CartItem doesn't just hold an ID — it holds a full Plant object.
 *  This is called COMPOSITION: one class contains another class.
 *
 *  This means we can write:
 *      cartItem.getPlant().getName()   → "Aloe Vera"
 *      cartItem.getPlant().getPrice()  → 20.00
 *
 *  This is much more expressive than storing just a plantId integer.
 *
 *  OOP CONCEPT: ENCAPSULATION
 *  --------------------------
 *  Private fields, public getters/setters.
 *  We also add a computed method getSubtotal() — a behaviour
 *  that belongs naturally on this class.
 * ============================================================
 */
public class CartItem {

    // -------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------
    private int   id;
    private int   userId;
    private Plant plant;     // Composition: CartItem HAS-A Plant
    private int   quantity;

    // -------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------
    public CartItem(int id, int userId, Plant plant, int quantity) {
        this.id       = id;
        this.userId   = userId;
        this.plant    = plant;
        this.quantity = quantity;
    }

    public CartItem() {}

    // -------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public Plant getPlant() {
        return plant;
    }

    public int getQuantity() {
        return quantity;
    }

    // -------------------------------------------------------
    // COMPUTED METHOD — This is a BEHAVIOUR of the CartItem
    // It calculates the subtotal (price × quantity)
    // Having this here keeps logic in one place (good OOP practice)
    // -------------------------------------------------------
    public double getSubtotal() {
        return plant.getPrice() * quantity;
    }

    // -------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItem{plant=" + plant.getName() + ", qty=" + quantity +
               ", subtotal=GH₵" + getSubtotal() + "}";
    }
}

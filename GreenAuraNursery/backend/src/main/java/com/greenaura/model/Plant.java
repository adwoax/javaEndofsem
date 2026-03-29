package com.greenaura.model;

/**
 * ============================================================
 *  Plant.java — Model Class (Represents a Plant Product)
 * ============================================================
 *
 *  OOP CONCEPT: CLASS & OBJECT
 *  ---------------------------
 *  Just like User.java, this class is a blueprint for a Plant.
 *  Every row in the `plants` database table becomes a Plant object
 *  in our Java code.
 *
 *  Example of creating a Plant object:
 *      Plant p = new Plant(1, "Aloe Vera", "Great for skin", 20.00, "aloe.jpg");
 *
 *  OOP CONCEPT: ENCAPSULATION
 *  --------------------------
 *  All fields are private. Access is only through getters/setters.
 *  This keeps the data safe and controlled.
 * ============================================================
 */
public class Plant {

    // -------------------------------------------------------
    // PRIVATE FIELDS — Encapsulation in action
    // -------------------------------------------------------
    private int    id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    // -------------------------------------------------------
    // CONSTRUCTOR — creates a complete Plant object at once
    // -------------------------------------------------------
    public Plant(int id, String name, String description,
                 double price, String imageUrl) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.price       = price;
        this.imageUrl    = imageUrl;
    }

    // Default (empty) constructor
    public Plant() {}

    // -------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // -------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        // Example of validation inside a setter:
        // We don't allow negative prices
        if (price < 0) {
            System.out.println("WARNING: Price cannot be negative. Setting to 0.");
            this.price = 0;
        } else {
            this.price = price;
        }
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Plant{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}

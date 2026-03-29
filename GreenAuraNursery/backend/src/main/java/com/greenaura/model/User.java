package com.greenaura.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ============================================================
 *  User.java — Model Class (Represents a Customer)
 * ============================================================
 *
 *  OOP CONCEPT: CLASS & OBJECT
 *  ---------------------------
 *  A CLASS is a blueprint. An OBJECT is a real instance of it.
 *  Example:
 *      User customer = new User();   ← This creates an OBJECT
 *      customer.setFullName("Nana"); ← We are using the object
 *
 *  OOP CONCEPT: ENCAPSULATION ⭐ (Most important here)
 *  ---------------------------------------------------
 *  Encapsulation means:
 *    1. Making fields PRIVATE so nothing outside can touch them directly
 *    2. Providing PUBLIC getters (to READ) and setters (to WRITE)
 *
 *  WHY? It protects the data. For example, you can add validation
 *  inside a setter so no one accidentally sets an invalid value.
 *
 *  Example of what Encapsulation PREVENTS:
 *      user.email = "not-an-email";   ← BAD: direct access (if public)
 *
 *  Example of what Encapsulation ALLOWS:
 *      user.setEmail("nana@gmail.com"); ← GOOD: controlled access
 * ============================================================
 */
public class User {

    // -------------------------------------------------------
    // PRIVATE FIELDS — cannot be accessed directly from outside
    // This is the core of Encapsulation
    // -------------------------------------------------------
    private int    id;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;

    // -------------------------------------------------------
    // CONSTRUCTOR — used to create a User object with all fields
    // at once (e.g., after reading from the database)
    // -------------------------------------------------------
    public User(int id, String fullName, String email,
                String password, String phone, String address) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
        this.address  = address;
    }

    // Also allow creating an empty User and filling it later
    public User() {}

    // -------------------------------------------------------
    // GETTERS — public methods that READ the private fields
    // -------------------------------------------------------

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    // -------------------------------------------------------
    // SETTERS — public methods that WRITE to the private fields
    // You could add validation here (e.g., check email format)
    // -------------------------------------------------------

    public void setId(int id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // -------------------------------------------------------
    // toString() — useful for printing/debugging
    // -------------------------------------------------------
    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + "', email='" + email + "'}";
    }
}

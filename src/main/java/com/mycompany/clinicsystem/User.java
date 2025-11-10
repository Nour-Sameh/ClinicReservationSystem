package com.mycompany.clinicsystem;
import java.util.*;

/**
 * Abstract class representing a generic user in the clinic system.
 * Provides common attributes such as ID, name, phone, email, and password.
 * Subclasses like Patient and Practitioner will inherit these properties.
 */
public abstract class User {
    protected int ID;
    protected String name;
    protected String phone;
    protected String email;
    protected String password;

    // Constructor: creates a new user with the specified personal information
    public User(int ID, String name, String phone, String email, String password) {
        this.ID = ID;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    // Returns the user's ID
    public int getID() { return ID; }

    // Returns the user's name
    public String getName() { return name; }

    // Returns the user's phone number
    public String getPhone() { return phone; }

    // Returns the user's email
    public String getEmail() { return email; }

    // Returns the user's password
    public String getPassword() { return password; }

    // Sets the user's name
    public void setName(String name) { this.name = name; }

    // Sets the user's phone number
    public void setPhone(String phone) { this.phone = phone; }

    // Sets the user's email
    public void setEmail(String email) { this.email = email; }

    // Sets the user's password
    public void setPassword(String password) { this.password = password; }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.util.*;
/**
 * Represents a department in the clinic system, holding a list of clinics
 * and its basic information like ID and name.
 * @author Javengers
 */
public class Department {
   
    private int ID;
    private String name;
    private List<Clinic> clinics = new ArrayList<>();

    // Constructor: creates a department with a given ID and name
    public Department(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    // Returns the department ID
    public int getID() {
        return ID;
    }
    public void addClinic(Clinic c) { clinics.add(c); }

    // Sets the department ID
    public void setID(int ID) {
        this.ID = ID;
    }

    // Returns the department name
    public String getName() {
        return name;
    }

    // Sets the department name
    public void setName(String name) {
        this.name = name;
    }

    // Returns the list of clinics in this department
    public List<Clinic> getClinics() {
        return clinics;
    }

    // Sets the list of clinics in this department
    public void setClinics(List<Clinic> clinics) {
        this.clinics = clinics;
    }
}


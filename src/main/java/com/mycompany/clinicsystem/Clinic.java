/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clinicsystem;
import java.util.*;

/**
 * Represents a medical clinic that belongs to a specific department.
 * Each clinic has identifying details, a pricing model, an associated schedule,
 * and collections of appointments and ratings.
 * 
 * This class is responsible for managing its schedule, appointments, and ratings,
 * as well as calculating its average rating.
 * 
 * @author Javengers
 */

 
public class Clinic {
    //Unique identifier for the clinic.
    private int ID;
    //The department ID this clinic belongs to.
    private int departmentID;
    //The clinic's display name. 
    private String name;
    //The physical address of the clinic.
    private String address;
    //The base consultation price for the clinic
    private double price;
    //The working schedule containing available time slots.
    private Schedule schedule;
    //The average rating of the clinic based on patient feedback. 
    private double avgRating;
    //The list of appointments booked in this clinic. 
    private List<Appointment> appointments ;
    //The list of ratings submitted by patients for this clinic.
    private List<Rating>ratings ;
    
    //Returns all ratings associated with this clinic.
    public List<Rating> getRatings() {
        return ratings;
    }
    //Adds a new rating to the clinic’s list of ratings.
    public void addToRatings(Rating x) {
        this.ratings.add(x);
    }

    public Clinic(int ID, int departmentID, String name, String address, double price, Schedule schedule) {
        this.ID = ID;
        this.departmentID = departmentID;
        this.name = name;
        this.address = address;
        this.price = price;
        this.schedule = schedule;
        this.appointments = new ArrayList();
        this.ratings = new ArrayList();
    }
    //Returns the unique identifier of the clinic.
    public int getID() {
        return ID;
    }
    //Sets the clinic’s unique identifier.
    public void setID(int ID) {
        this.ID = ID;
    }
    //Returns the department ID associated with this clinic.
    public int getDepartmentID() {
        return departmentID;
    }
    //Updates the department ID of the clinic.
    public void setDepartmentID(int departmentID) {
        this.departmentID = departmentID;
    }
    //Returns the name of the clinic.
    public String getName() {
        return name;
    }
    // Updates the clinic name.
    public void setName(String name) {
        this.name = name;
    }
    //Returns the address of the clinic
    public String getAddress() {
        return address;
    }
    //Updates the address of the clinic.
    public void setAddress(String address) {
        this.address = address;
    }
    //Returns the base consultation price of the clinic.
    public double getPrice() {
        return price;
    }
    // Sets the base consultation price of the clinic.
    public void setPrice(double price) {
        this.price = price;
    }
    //Returns the clinic's current schedule.
    public Schedule getSchedule() {
        return schedule;
    }
    //Sets a new schedule for this clinic and regenerates time slots
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        schedule.generateTimeSlots();
    }

    //Calculates and returns the average rating of the clinic.
     /* 
      This method iterates through all ratings, sums the scores,
      and divides by the total number of ratings to compute the mean value.*/
    public double getAvgRating() {
        double r = 0;
        for(Rating x : ratings) {
            r += x.getScore();
        }
        return r/ratings.size();
    }
    
    //Sets the average rating value of the clinic manually.
    public void setAvgRating(double avgRating) {    
        this.avgRating = avgRating;
    }
    
    //Adds a new schedule to the clinic
    public void addSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    //Returns the list of appointments associated with this clinic.
    public List<Appointment> getAppointments() {
        return appointments;
    }
    //Returns a list of currently available time slots.
     
     /* This method compares all total slots in the schedule against
      the slots already booked by appointments, and returns only
      the unbooked (available) slots.*/
    
    public List<TimeSlot> getAvailableSlot() {
        List<TimeSlot> total = schedule.getSlots();
        List<TimeSlot> Booked = new ArrayList();
        for(Appointment x: appointments){
            Booked.add(x.getAppointmentDateTime());
        }
        
        List<TimeSlot> r = new ArrayList();
        for(TimeSlot x : total) {
            if(!Booked.contains(x)) {
                r.add(x);
            }
        }
        return r;
    }
    
    
}


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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

    private int ID;
    private int departmentID;
    private String name;
    private String address;
    private double price;
    private Schedule schedule;
    private double avgRating;
    private List<Appointment> appointments ;
    private List<Rating>ratings ;
    private Queue<WaitingList> waitingList = new LinkedList<>();
    
    
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
}

    public Queue<WaitingList> getWaitingList() {
        return waitingList;
    }

    //Calculates and returns the average rating of the clinic.
     /* 
      This method iterates through all ratings, sums the scores,
      and divides by the total number of ratings to compute the mean value.*/
    public double getAvgRating() {
        if (ratings.isEmpty()) return 0;
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
    
    public void cancelAppointmentsInDay(DayOfWeek day) {
        List<Appointment> toCancel = new ArrayList<>();
        for(Appointment a : appointments) {
            if(a.getAppointmentDateTime().getDay() == day) {
                toCancel.add(a);
            }
        }
        for(Appointment a : toCancel) {
            a.cancel();
            appointments.remove(a);
            System.out.println("Appointment for " + a.getPatient().getName() + " cancelled on " + day);
        }
    }
    
    public void addToWaitingList(Patient patient) {
        waitingList.add(new WaitingList(patient,this));
    }
    
    public void notifyWaitingList(TimeSlot freedSlot) {
        if(waitingList.isEmpty()) return;

        WaitingList next = waitingList.poll();
        Patient p = next.getPatient();

        // الفكرة هنا: تبعتي Notification (GUI message أو Email)
        System.out.println("Notification to " + p.getEmail() +
                           ": A slot became available at " + freedSlot +
                           ". Would you like to book it?");

    }
    
    public void shiftSchedule(int minutes) {
        for (TimeSlot slot : schedule.getSlots()) {
            LocalTime newStart = slot.getStartTime().plusMinutes(minutes);
            LocalTime newEnd = slot.getEndTime().plusMinutes(minutes);

            // لو خرج برا اليوم → يتكنسل
            if (newStart.isBefore(LocalTime.MIN) || newEnd.isAfter(LocalTime.MAX)) {
                for (Appointment app : appointments) {
                    if (app.getAppointmentDateTime().equals(slot)) {
                        app.cancel();
                    }
                }
                continue;
            }

            slot.setStartTime(newStart);
            slot.setEndTime(newEnd);
        }
    }

    public void removeDay(DayOfWeek day) {
        // 1) امسحي كل ال Slots لهذا اليوم
        List<TimeSlot> removedSlots = schedule.getSlots().stream()
                .filter(slot -> slot.getDay() == day)
                .toList();

        // 2) الغي كل المواعيد اللي في السلوْتز دي
        for (Appointment app : new ArrayList<>(appointments)) {
            if (removedSlots.contains(app.getAppointmentDateTime())) {
                app.cancel();  // هتحددها Cancel + تشيلها من patient/clinic
            }
        }

        // 3) شيل السلوْتز نهائيًا
        schedule.getSlots().removeIf(slot -> slot.getDay() == day);
    }
    public void regenerateSlots(LocalDate start, LocalDate end) {
        List<TimeSlot> oldSlots = new ArrayList<>(schedule.getSlots());

        schedule.generateTimeSlots(start, end);

        List<TimeSlot> newSlots = schedule.getSlots();

        for (Appointment app : new ArrayList<>(appointments)) {
            if (!newSlots.contains(app.getAppointmentDateTime())) {
                app.cancel();
            }
        }
    }

    
}



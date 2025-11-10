package com.mycompany.clinicsystem;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a time slot for appointments, including day, start and end times,
 * and whether the slot is booked or available.
 * @author Javengers
 */
public class TimeSlot {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;

    // Default constructor: creates an empty time slot
    public TimeSlot() {
    }
    
    // Constructor: creates a time slot with start and end times and booking status
    public TimeSlot(LocalTime startTime, LocalTime endTime, boolean isBooked) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
    }

    // Constructor: creates a time slot with day, start and end times (initially available)
    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Returns the day of the week for this time slot
    public DayOfWeek getDay() {
        return day;
    }

    // Sets the day of the week for this time slot
    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    // Returns the start time of the time slot
    public LocalTime getStartTime() {
        return startTime;
    }

    // Returns the end time of the time slot
    public LocalTime getEndTime() {
        return endTime;
    }

    // Returns whether the time slot is booked
    public boolean isBooked() {
        return isBooked;
    }

    // Sets the start time of the time slot
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    // Sets the end time of the time slot
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    // Marks the time slot as booked
    public void markAsBooked() {
        isBooked = true;
    }
    
    // Marks the time slot as available
    public void markAsAvailable() {
        isBooked = false;
    }

    // Returns a string representation of the time slot details
    @Override
    public String toString() {
        return "TimeSlot{" + "day=" + day + ", startTime=" + startTime + ", endTime=" + endTime + ", isBooked=" + isBooked + '}';
    }
    
}

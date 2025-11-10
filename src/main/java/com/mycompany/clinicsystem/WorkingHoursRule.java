package com.mycompany.clinicsystem;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a working hours rule for a clinic or practitioner,
 * specifying the day and the start and end times of work.
 * @author Javengers
 */
public class WorkingHoursRule {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endtTime;

    // Constructor: creates a working hours rule with a specific day, start time, and end time
    public WorkingHoursRule(DayOfWeek day, LocalTime startTime, LocalTime endtTime) {
        this.day = day;
        this.startTime = startTime;
        this.endtTime = endtTime;
    }

    // Returns the day of the week for this working hours rule
    public DayOfWeek getDay() {
        return day;
    }

    // Returns the start time of the working hours
    public LocalTime getStartTime() {
        return startTime;
    }

    // Returns the end time of the working hours
    public LocalTime getEndtTime() {
        return endtTime;
    }

    // Sets the day of the week for this working hours rule
    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    // Sets the start time of the working hours
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    // Sets the end time of the working hours
    public void setEndtTime(LocalTime endtTime) {
        this.endtTime = endtTime;
    }

    // Returns a string representation of the working hours rule
    @Override
    public String toString() {
        return "WorkingHoursRule{" + "day=" + day + ", startTime=" + startTime + ", endtTime=" + endtTime + '}';
    }
    
}

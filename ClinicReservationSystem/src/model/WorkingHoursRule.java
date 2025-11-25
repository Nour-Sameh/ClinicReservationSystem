/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a working hours rule for a clinic or practitioner,
 * specifying the day and the start and end times of work.
 * @author Javengers
 */
public class WorkingHoursRule {
    private int id; 
    private DayOfWeek day;
    private int scheduleId; 
    private LocalTime startTime;
    private LocalTime endTime;

    // Constructor: creates a working hours rule with a specific day, start time, and end time
    public WorkingHoursRule(int id, int scheduleId, DayOfWeek day, LocalTime startTime, LocalTime endtTime) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endtTime;
    }
    public WorkingHoursRule(int scheduleId, DayOfWeek day, LocalTime startTime, LocalTime endtTime) {
        this(0, scheduleId, day, startTime, endtTime);
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndtTime() { return endTime; }

    public void setDay(DayOfWeek day) { this.day = day; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndtTime(LocalTime endtTime) { this.endTime = endtTime; }
    
    // Returns a string representation of the working hours rule
    @Override
    public String toString() {
        return "WorkingHoursRule{" + "day=" + day + ", startTime=" + startTime + ", endtTime=" + endTime + '}';
    }
    
}


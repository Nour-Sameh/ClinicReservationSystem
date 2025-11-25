/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class TimeSlot {
    
    private int id; 
    private int clinicId; //  تم إضافته
    private LocalDate date;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;
    private boolean isCancelled;

    public TimeSlot( LocalDate date, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = false;
        this.isCancelled = false;
    }

    public TimeSlot(int id, int clinicId, LocalDate date, DayOfWeek day, LocalTime startTime, LocalTime endTime, boolean isBooked, boolean isCancelled) {
        this.id = id;
        this.clinicId = clinicId;
        this.date = date;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
        this.isCancelled = isCancelled;
    }
    
    // Getters and Setters:
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }
    
    public LocalDate getDate() { return date; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isBooked() { return isBooked; }
    public boolean isCancelled() { return isCancelled; }
    
    
    public void markAsBooked() { isBooked = true; isCancelled = false; }
    public void markAsAvailable() { isBooked = false; isCancelled = false; }
    public void markAsCancelled() { isBooked = false; isCancelled = true; }

    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot t = (TimeSlot) o;
        return date.equals(t.date) &&
                startTime.equals(t.startTime) &&
                endTime.equals(t.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", clinicId=" + clinicId +
                ", date=" + date +
                ", day=" + day +
                ", start=" + startTime +
                ", end=" + endTime +
                ", booked=" + isBooked +
                ", cancelled=" + isCancelled +
                '}';
    }
}
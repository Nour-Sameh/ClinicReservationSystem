package com.mycompany.clinicsystem;
import java.time.LocalDate;

/**
 * @author mariam
 */
public class TimeSlot {
    private LocalDate startTime;
    private LocalDate endTime;
    private boolean isBooked;

    public TimeSlot() {
    }
    
    public TimeSlot(LocalDate startTime, LocalDate endTime, boolean isBooked) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public boolean isIsBooked() {
        return isBooked;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }
    
    public void markAsBooked() {
        isBooked = true;
    }
    
    public void markAsAvailable() {
        isBooked = false;
    }
}
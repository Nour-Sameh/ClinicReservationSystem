package com.mycompany.clinicsystem;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * @author mariam
 */
public class WorkingHoursRule {
    private int ID;
    private DayOfWeek day;
    private LocalDate startTime;
    private LocalDate endtTime;

    public int getID() {
        return ID;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public LocalDate getEndtTime() {
        return endtTime;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public void setEndtTime(LocalDate endtTime) {
        this.endtTime = endtTime;
    }
    
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinicreservationsystem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Represents a time slot for appointments, including day, start and end times,
 * and whether the slot is booked or available.
 * @author Javengers
 */
public class TimeSlot {
    private LocalDate date;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;
    private boolean isCancelled;

    public TimeSlot(LocalDate date, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = false;
        this.isCancelled = false;
    }

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
                "date=" + date +
                ", day=" + day +
                ", start=" + startTime +
                ", end=" + endTime +
                ", booked=" + isBooked +
                ", cancelled=" + isCancelled +
                '}';
    }
}

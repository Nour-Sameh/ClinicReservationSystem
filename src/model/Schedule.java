/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a schedule for a clinic or practitioner, including the slot duration,
 * weekly working hours rules, and generated time slots.
 * @author Javengers
 */
public class Schedule {
    private int ID;
    private int slotDurationInMinutes;
    private List<WorkingHoursRule> weeklyRules;
    private List<TimeSlot> slots;

    // Constructor: creates a schedule with an ID, slot duration, and weekly working hours rules
    public Schedule(int ID, int slotDurationInMinutes, List<WorkingHoursRule> weeklyRules) {
        this.ID = ID;
        this.slotDurationInMinutes = slotDurationInMinutes;
        this.weeklyRules = weeklyRules;
        this.slots = new ArrayList<>();
    }
    public Schedule(int ID, int slotDurationInMinutes) {
        this.ID = ID;
        this.slotDurationInMinutes = slotDurationInMinutes;
    }

    // Returns the list of time slots in the schedule
    public List<TimeSlot> getSlots() {
        return slots;
    }

    // Sets the list of time slots in the schedule
    public void setSlots(List<TimeSlot> slots) {
        this.slots = slots;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    
    // Returns the schedule ID
    public int getID() {
        return ID;
    }

    // Returns the duration of each slot in minutes
    public int getSlotDurationInMinutes() {
        return slotDurationInMinutes;
    }

    // Sets the duration of each slot in minutes
    public void setSlotDurationInMinutes(int slotDurationInMinutes) {
        this.slotDurationInMinutes = slotDurationInMinutes;
    }

    // Returns the weekly working hours rules
    public List<WorkingHoursRule> getWeeklyRules() {
        return weeklyRules;
    }

    // Sets the weekly working hours rules
    public void setWeeklyRules(List<WorkingHoursRule> weeklyRules) {
        this.weeklyRules = weeklyRules;
    }
    
    // Generates the time slots for the schedule based on the weekly rules and slot duration
    public void generateTimeSlots(LocalDate startDate, LocalDate endDate) {
        slots.clear();
        if (weeklyRules == null || weeklyRules.isEmpty()) return;

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            for (WorkingHoursRule rule : weeklyRules) {
                if (rule.getDay() == day) {
                    LocalTime start = rule.getStartTime();
                    LocalTime end = rule.getEndtTime();
                    LocalTime time = start;

                    while (!time.isAfter(end.minusMinutes(slotDurationInMinutes))) {
                        LocalTime slotEnd = time.plusMinutes(slotDurationInMinutes);
                        TimeSlot slot = new TimeSlot(currentDate, day, time, slotEnd);
                        slots.add(slot);
                        time = slotEnd;
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }


}
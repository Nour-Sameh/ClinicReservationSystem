package com.mycompany.clinicsystem;
import java.time.DayOfWeek;
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

    // Returns the list of time slots in the schedule
    public List<TimeSlot> getSlots() {
        return slots;
    }

    // Sets the list of time slots in the schedule
    public void setSlots(List<TimeSlot> slots) {
        this.slots = slots;
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
    public void generateTimeSlots() {
        slots.clear();

        if (weeklyRules == null || weeklyRules.isEmpty()) {
            System.out.println(" No working hours defined yet.");
            return;
        }

        for (WorkingHoursRule rule : weeklyRules) {
            DayOfWeek day = rule.getDay();
            LocalTime start = rule.getStartTime();
            LocalTime end = rule.getEndtTime();

            LocalTime current = start;

            while (current.plusMinutes(slotDurationInMinutes).isBefore(end) ||
                   current.plusMinutes(slotDurationInMinutes).equals(end)) {

                LocalTime slotEnd = current.plusMinutes(slotDurationInMinutes);

                TimeSlot slot = new TimeSlot(day, current, slotEnd);

                slots.add(slot);

                current = slotEnd;
            }
        }
    }

}

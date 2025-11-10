/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.clinicsystem;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 *
 * @author Javengers 
 */
/**
 * Main class representing the Clinic Management System.
 * Handles user login/registration, practitioner and patient menus,
 * clinic management, appointment booking, and ratings.
 */
public class ClinicSystem {
    
    private static final Scanner in = new Scanner(System.in);
    private static  Practitioner cur_Practitioner = null;
    private static  Patient cur_Patient = null;
    private static List<Practitioner> Practitioners;
    private static List<Patient> patients;
    private static List<Clinic> clinics;
    private static List<Appointment> appointments;
    
    
    public static void main(String[] args) {
        
        
        clinics = new ArrayList<>();
        appointments = new ArrayList<>();

        Practitioners = new ArrayList<>();
        patients = new ArrayList<>();
        start();
        
    }
     // Starts the main system loop (login/register/exit)
    private static void start() {
        
        while (true) {
            System.out.println("=== Welcome to Weekly Clinic System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("> ");
            int choice = in.nextInt();
            in.nextLine();

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    Register();
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
     // Handles user login for both practitioners and patients
    static void login() {
        
        System.out.println("-- Login --");
        System.out.print("email: ");
        String email = in.nextLine();
        System.out.print("Password: ");
        String password = in.nextLine();
        //in.nextLine();

        for (Practitioner doc : Practitioners) {
            if (doc.email.equals(email) && doc.password.equals(password)) {
                cur_Practitioner = doc;
                break;
            }
        }
        if (cur_Practitioner != null) {
            doctorMenu();
            return;
        }

        for (Patient pat : patients) {
            if (pat.email.equals(email) && pat.password.equals(password)) {
                cur_Patient = pat;
                break;
            }
        }

        if (cur_Patient != null) {
            patientMenu();
            return;
        } 
        else {
            System.out.println("Invalid login ");
            login();
        }
        
    }
    
// Handles user registration for practitioners or patients
    
    private static void Register() {
        
        System.out.println("-- Register --");
        System.out.println("Choose Role: ");
        System.out.println("1. Doctor");
        System.out.println("2. Patient");
        System.out.print("> ");
        int role = in.nextInt();
        in.nextLine();

        while(role != 1 && role != 2) {
            System.out.println("Invalid role! Must be 1 or 2.");
            role = in.nextInt();
            in.nextLine();
        }

        System.out.print("Enter name: ");
        String name = in.nextLine().trim();
        while(name.isEmpty() || name.length() < 3 || !name.matches("[a-zA-Z ]+")) { 
            System.out.println("Name must be at least 3 characters and contain letters only.");
            System.out.print("Enter name: ");
            name = in.nextLine().trim();
        }

        System.out.print("Enter phone (Egyptian): ");
        String phone = in.nextLine().trim();
        while(!phone.matches("^(010|011|012|015)[0-9]{8}$")) {
            System.out.println(" Invalid phone number format.");
             System.out.print("Enter phone (Egyptian): ");
            phone = in.nextLine().trim();
        }

        System.out.print("Enter email: ");
        String email = in.nextLine().trim();
        while(!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println(" Invalid email format.");
            System.out.print("Enter email: ");
            email = in.nextLine().trim();
        }
        
        while(emailExists(email)) {
            System.out.println(" Email already exists.");
            System.out.print("Enter email: ");
            email = in.nextLine().trim();
            while(!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                System.out.println("❌ Invalid email format.");
                System.out.print("Enter email: ");
                email = in.nextLine().trim();
            }   
        }
        
        System.out.print("Enter password: ");
        String password = in.nextLine().trim();
        while (password.length() < 4) {
            System.out.println(" Password must be at least 4 characters.");
            password = in.nextLine().trim();
        }

        int newID = generateID();

        if (role == 1){
            Clinic clinic = null;
            Practitioner doctor = new Practitioner(newID, name, phone, email, password, clinic);
            Practitioners.add(doctor);
            System.out.println(" Doctor registered successfully!");
        }
        else {
            Patient patient = new Patient(newID, name, phone, email, password);
            patients.add(patient);
            System.out.println(" Patient registered successfully!");
        }

        login();
    }
    
    // Checks if the email is already used
    
    private static boolean emailExists(String email) {
        for (Practitioner d : Practitioners)
            if (d.getEmail().equalsIgnoreCase(email)) return true;
        for (Patient p : patients)
            if (p.getEmail().equalsIgnoreCase(email)) return true;
        return false;
    }
    
    // Generates a random ID
    private static int generateID() {
        int newID;
        do {
            newID = new Random().nextInt(10000) + 1;
        } while (idExists(newID));
        return newID;
    }
    
    private static boolean idExists(int id) {
        for (Practitioner d : Practitioners) {
            if (d.getID() == id) return true;
        }
        for (Patient p : patients) {
            if (p.getID() == id) return true;
        }
        for (Clinic c : clinics) {
            if (c.getID() == id) return true;
        }
        return false;
    }

    
    
    // Practitioner menu
    
    private static void doctorMenu() {
        
        System.out.println("️ Welcome Dr. " + cur_Practitioner.name);
        System.out.println("1. Add Clinic");
        System.out.println("2. View Clinic");
        System.out.println("3. Logout");
        System.out.print("> ");
        int choice = in.nextInt();
        in.nextLine();
        
         switch (choice) {
            case 1:
                addClinic();
                break;
            case 2:
                viewClinic();
                break;
            case 3:
                cur_Practitioner = null;
                System.out.println("Logged out ");
                return;
            default:
                System.out.println(" Invalid choice");
        }
    }

    // Patient menu
    
    private static void patientMenu() {
        while (true) {
            System.out.println("\nPatient Menu:");
            System.out.println("1. Search Clinics by Specialty");
            System.out.println("2. View My Appointments");
            System.out.println("3. Add Rating");
            System.out.println("4. Logout");
            System.out.print("> ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1":
                    searchClinics();
                    break;
                case "2":
                    viewMyAppointments();
                    break;
                case"3":
                    addRating();
                    break;
                case "4":
                    cur_Patient = null;
                    System.out.println("Logged out ");
                    return;
                default:
                    System.out.println(" Invalid choice");
            }
        }
    }
    
    // ----------------------------------------------------------
    
    // Adds a new clinic for the current practitioner
        private static void addClinic() {
        System.out.println("\n-- Add Clinic --");
        int ID = generateID();

        //in.nextLine();
        System.out.print("Enter clinic name: ");
        String name = in.nextLine();

        System.out.print("Enter clinic price: ");
        double price = in.nextDouble();
        in.nextLine();

        System.out.print("Enter address: ");
        String address = in.nextLine();

        System.out.println("Choose specialty:");
        System.out.println("1. Cardiology");
        System.out.println("2. Dermatology");
        System.out.println("3. Pediatrics");
        System.out.println("4. General");
        System.out.print("> ");
        int choice = in.nextInt();
        in.nextLine();

        Clinic clinic = new Clinic(ID, choice, name, address, price, null);

        System.out.println("\nEnter slot duration in minutes:");
        int slotDurationInMinutes = in.nextInt();
        in.nextLine();

        Schedule schedule = new Schedule(generateID(), slotDurationInMinutes, null);
        List<WorkingHoursRule> rules = new ArrayList<>();

        while (true) {
            System.out.print("Add day? (Y/N): ");  // validation
            String ans = in.nextLine().trim().toUpperCase();
            if (ans.equals("N")) break;

            System.out.print("Day of week (e.g. MONDAY): ");
            DayOfWeek day = DayOfWeek.valueOf(in.nextLine().trim().toUpperCase());

            System.out.print("Start time (HH:MM): ");
            LocalTime start = LocalTime.parse(in.nextLine().trim());

            System.out.print("End time (HH:MM): ");
            LocalTime end = LocalTime.parse(in.nextLine().trim());

            rules.add(new WorkingHoursRule(day, start, end));
        }

        schedule.setWeeklyRules(rules);
        clinic.setSchedule(schedule);// time slots created
        cur_Practitioner.setClinic(clinic);
        clinics.add(clinic);
        System.out.println("\nClinic added successfully ");
        doctorMenu();
        
    }
    
    // Views the current practitioner's clinic details
        
    private static void viewClinic() {
        System.out.println("\n-- View Clinic --");

        if (cur_Practitioner.getClinic() == null) {
            System.out.println("You don't have a clinic yet.");
            
        }
        else{
            Clinic clinic = cur_Practitioner.getClinic();

            System.out.println("Name: " + clinic.getName());
            System.out.println("Address: " + clinic.getAddress());
            System.out.println("Price: " + clinic.getPrice());

            String specialty = switch (clinic.getDepartmentID()) {
                case 1 -> "Cardiology";
                case 2 -> "Dermatology";
                case 3 -> "Pediatrics";
                case 4 -> "General";
                default -> "Unknown";
            };
            System.out.println("Specialty: " + specialty);

            Schedule schedule = clinic.getSchedule();
            if (schedule == null) {
                System.out.println("\nNo schedule set for this clinic.");
            } else {
                System.out.println("\nSlot duration: " + schedule.getSlotDurationInMinutes() + " minutes");
                System.out.println("Working Days & Hours:");
                if (schedule.getWeeklyRules() == null || schedule.getWeeklyRules().isEmpty()) {
                    System.out.println("No working hours defined.");
                } else {
                    for (WorkingHoursRule rule : schedule.getWeeklyRules()) {
                        System.out.println("- " + rule.getDay() + ": " 
                            + rule.getStartTime() + " → " + rule.getEndtTime());
                    }
                }
            }
        }
        doctorMenu();
    }
    
    // Allows a patient to search clinics by specialty and book an appointment
    
    private static void searchClinics() {

        System.out.println("\n-- Search Clinics by Specialty --");
        System.out.println("1. Cardiology");
        System.out.println("2. Dermatology");
        System.out.println("3. Pediatrics");
        System.out.println("4. General");
        System.out.print("> ");
        int choice = in.nextInt();
        in.nextLine();

        List<Clinic> found = new ArrayList<>();

        for (Clinic c : clinics) {
            if (c != null && c.getDepartmentID() == choice)
                found.add(c);
        }

        if (found.isEmpty()) {
            System.out.println(" No clinics found for that specialty.");
            return;
        }

        System.out.println("\nAvailable Clinics:");
        for (int i = 0; i < found.size(); i++) {
            Clinic c = found.get(i);
            System.out.println((i + 1) + ". " + c.getName() + " | " + c.getAddress() + " | Price: " + c.getPrice() + " | Rating: " + c.getAvgRating());
        }

        System.out.print("Enter clinic number to book (0 to cancel): ");
        int index = in.nextInt();
        in.nextLine();
        if (index <= 0 || index > found.size()) return;

        Clinic selected = found.get(index - 1);

        if (selected.getSchedule() == null || selected.getSchedule().getWeeklyRules() == null) {
            System.out.println(" Clinic has no working schedule.");
            return;
        }
        
        System.out.println("\nAvailable Working Days:");
        for (WorkingHoursRule rule : selected.getSchedule().getWeeklyRules()) {
            System.out.println("- " + rule.getDay() + " (" + rule.getStartTime() + " - " + rule.getEndtTime() + ")");
        }
        boolean flag=true;
        DayOfWeek chosenDay = null;
        List<TimeSlot> availableSlots = new ArrayList<>();
        while(flag){
            System.out.print("Choose a day (e.g. MONDAY): ");
            String dayInput = in.nextLine().trim().toUpperCase();
            try {
                chosenDay = DayOfWeek.valueOf(dayInput);
            } catch (Exception e) {
                System.out.println("Invalid day name.");
                return;
            }
            for (TimeSlot slot : selected.getSchedule().getSlots()) {
                if (slot.getDay() == chosenDay && !slot.isBooked()) {
                    availableSlots.add(slot);
                }
            }

            if (availableSlots.isEmpty()) {
                System.out.println("No available slots on " + chosenDay + ".");  
            }
            else {
                flag=false;
            }
        }
        System.out.println("\nAvailable slots on " + chosenDay + ":");
        for (int i = 0; i < availableSlots.size(); i++) {
            TimeSlot s = availableSlots.get(i);
            System.out.println((i + 1) + ". " + s.getStartTime() + " - " + s.getEndTime());
        }

        System.out.print("Choose a slot number: ");
        int slotIndex = in.nextInt();
        in.nextLine();

        if (slotIndex <= 0 || slotIndex > availableSlots.size()) return;

        TimeSlot chosenSlot = availableSlots.get(slotIndex - 1);
        chosenSlot.markAsBooked();

        Appointment newApp = new Appointment(cur_Patient, selected, chosenSlot);
        appointments.add(newApp);

        System.out.println(" Appointment booked successfully for " + chosenDay + " " +
                            chosenSlot.getStartTime() + " - " + chosenSlot.getEndTime());
    }
  // Views all appointments of the current patient
    
    private static void viewMyAppointments() {
        System.out.println("\n-- My Appointments --");

        boolean foundAny = false;

        for (Appointment a : appointments) {
            if (a.getPatient() == cur_Patient) {
                foundAny = true;
                System.out.println(" Clinic: " + a.getClinic().getName() +
                    " | Day/Time: " + a.getAppointmentDateTime().toString());
            }
        } 
        if (!foundAny)
            System.out.println("No appointments found.");
    }
    
    // Allows a patient to add a rating for a clinic
    
     static void addRating() {
        if (clinics.isEmpty()) {
            System.out.println("No clinics available to rate.");
            return;
        }
        else {
            System.out.println("\n-- Rate a Clinic --");
            for (int i = 0; i < clinics.size(); ++i) {
                Clinic c = clinics.get(i);
                System.out.println((i + 1) + ". " + c.getName() + " | " + c.getAddress() + " | Price: " + c.getPrice());
            }

            int choice;
            System.out.print("Select clinic number to rate: ");
            choice = in.nextInt();
            in.nextLine();

            while (choice < 1 || choice > clinics.size()) {
                System.out.println("Invalid choice!");
                choice = in.nextInt();
                in.nextLine();
            }

            Clinic selectedClinic = clinics.get(choice - 1);

            System.out.print("Enter rating score (1–5): ");
            int score = in.nextInt();
            in.nextLine(); 
            while (score < 1 || score > 5) {
                System.out.println("Invalid score!");
                score = in.nextInt();
                in.nextLine();
            }

            System.out.print("Enter your comment: ");
            String comment = in.nextLine();

            if (cur_Patient != null) {
                cur_Patient.addRating(selectedClinic, score, comment);
                System.out.println(" Rating added successfully for " + selectedClinic.getName());
            } 
            else {
                System.out.println("You must be logged in as a patient to rate a clinic.");
            }
        }
        
    }    

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.clinicsystem;
import java.util.*;

/**
 *
 * @author noursameh                              == validate before switch 
 */
public class ClinicSystem {
    
    private static final Scanner in = new Scanner(System.in);
    private static  Practitioner cur_Practitioner = null;
    private static  Patient cur_Patient = null;
    private static List<Practitioner> Practitioners;
    private static List<Patient> patients;
    
    public static void main(String[] args) {
        Practitioners = new ArrayList<>();
        patients = new ArrayList<>();
        start();
        
    }
    
    private static void start() {
        
        while (true) {
            System.out.println("=== Welcome to Weekly Clinic System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            int choice = in.nextInt();
            //scanner.nextLine();

            switch (choice) {
                case 1:
                    login();
                    start();/////////////////////
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
    static void login() {
        
        System.out.println("-- Login --");
        System.out.print("email: ");
        String email = in.nextLine();
        System.out.print("Password: ");
        String password = in.nextLine();

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
            System.out.println("Invalid login ❌");
        }
        
}

    private static void Register() {
        
        System.out.println("-- Register --");
        System.out.println("Choose Role: ");
        System.out.println("1. Doctor");
        System.out.println("2. Patient");
        System.out.print("> ");
        int role = in.nextInt();
        in.nextLine();

        while(role != 1 && role != 2) {
            System.out.println("❌ Invalid role! Must be 1 or 2.");
            role = in.nextInt();
            in.nextLine();
        }

        System.out.print("Enter name: ");
        String name = in.nextLine().trim();
        while(name.isEmpty() || name.length() < 3) {
            System.out.println("❌ Name must be at least 3 characters.");
            System.out.print("Enter name: ");
            name = in.nextLine().trim();
        }

        System.out.print("Enter phone (Egyptian): ");
        String phone = in.nextLine().trim();
        while(!phone.matches("^(010|011|012|015)[0-9]{8}$")) {
            System.out.println("❌ Invalid phone number format.");
             System.out.print("Enter phone (Egyptian): ");
            phone = in.nextLine().trim();
        }

        System.out.print("Enter email: ");
        String email = in.nextLine().trim();
        while(!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println("❌ Invalid email format.");
            System.out.print("Enter email: ");
            email = in.nextLine().trim();
        }
        
        while(emailExists(email)) {
            System.out.println("❌ Email already exists.");
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
            System.out.println("❌ Password must be at least 4 characters.");
            password = in.nextLine().trim();
        }

        int newID = generateID(); // validiation

        if (role == 1){
            Clinic clinic = null;
            Practitioner doctor = new Practitioner(newID, name, phone, email, password, clinic);
            Practitioners.add(doctor);
            System.out.println("✅ Doctor registered successfully!");
        }
        else {
            Patient patient = new Patient(newID, name, phone, email, password);
            patients.add(patient);
            System.out.println("✅ Patient registered successfully!");
        }

        login();
    }
    
    private static boolean emailExists(String email) {
        for (Practitioner d : Practitioners)
            if (d.getEmail().equalsIgnoreCase(email)) return true;
        for (Patient p : patients)
            if (p.getEmail().equalsIgnoreCase(email)) return true;
        return false;
    }
    
    private static int generateID() {
        return new Random().nextInt(10000) + 1;
    }
    
    private static void doctorMenu() {
        
        System.out.println("️ Welcome Dr. " + cur_Practitioner.name);
        System.out.println("1. Add Clinic");
        System.out.println("2. View Clinic");
        System.out.println("3. Logout");
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
                System.out.println("Logged out ✅");
                return;
            default:
                System.out.println("❌ Invalid choice");
        }
    }

    private static void patientMenu() {
        while (true) {
            System.out.println("\nPatient Menu:");
            System.out.println("1. Search Clinics by Specialty");
            System.out.println("2. View My Appointments");
            System.out.println("3. Logout");
            System.out.print("> ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1":
                    searchClinics();
                    break;
                case "2":
                    viewMyAppointments();
                    break;
                case "3":
                    cur_Patient = null;
                    System.out.println("Logged out ✅");
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }
    
    // ----------------------------------------------------------
    
    private static void addClinic() {
        
        System.out.println("\n-- Add Clinic --");
        System.out.print("Enter clinic name: ");
        String name = in.nextLine();

        System.out.println("Choose specialty:");
        System.out.println("1. Cardiology");
        System.out.println("2. Dermatology");
        System.out.println("3. Pediatrics");
        System.out.print("> ");
        String spChoice = in.nextLine().trim();
        String specialty = switch (spChoice) {
            case "1" -> "Cardiology";
            case "2" -> "Dermatology";
            case "3" -> "Pediatrics";
            default -> "General";
        };

        System.out.print("Enter location: ");
        String location = in.nextLine();
        
        System.out.print("Enter slot duration (minutes): ");
        int slotDuration = Integer.parseInt(in.nextLine());

        Clinic clinic = new Clinic(name, specialty, location, slotDuration);
        curDoctor.addClinic(clinic);

        System.out.println("Clinic created successfully ✅");

        // Set availability
        setAvailability(clinic);

        System.out.println("System will auto-generate weekly slots.");
        System.out.println("(Generated sample slots for each day)");

        System.out.println("\n✅ Clinic setup complete!");
    }
    
    private static void setAvailability(Clinic clinic) {
        while (true) {
            System.out.print("\nAdd day? (Y/N): ");
            String ans = in.nextLine().trim().toUpperCase();
            if (ans.equals("N")) break;

            System.out.print("Day of week: ");
            String day = in.nextLine().trim();

            System.out.print("Start time (e.g. 09:00): ");
            String start = in.nextLine().trim();

            System.out.print("End time (e.g. 13:00): ");
            String end = in.nextLine().trim();

            clinic.addAvailability(new Availability(day, start, end));
            System.out.println("Added availability: " + day + " " + start + "-" + end);
        }
        System.out.println("Availability saved ✅");
    }

}

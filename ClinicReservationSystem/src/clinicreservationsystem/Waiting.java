/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinicreservationsystem;
import java.util.*;

/**
 *
 * @author noursameh
 */
public class Waiting {
    private Patient patient;
    private Date requestTime;

    public Waiting(Patient patient) {
        this.patient = patient;
        this.requestTime = new Date();
    }

    public Patient getPatient() {
        return patient;
    }

    public Date getRequestTime() {
        return requestTime;
    }
}
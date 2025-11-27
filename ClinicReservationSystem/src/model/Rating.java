/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


public class Rating {  
    private int id;
    private Patient patient; 
    private Clinic clinic;   
    private int score;       // Numerical score (1–5)
    private String comment;  

    // Constructor: creates a new rating with patient, clinic, score, and comment
    public Rating(int id, Patient patient, Clinic clinic, int score, String comment){
        this.id = id;
        this.patient = patient;
        this.clinic = clinic;
        this.score = score;
        this.comment = comment;
    }

    public Rating(Patient patient, Clinic clinic, int score, String comment){
        this(0, patient, clinic, score, comment);
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public Patient getPatient() { return patient; }
    public Clinic getClinic() { return clinic; }
    public int getScore() { return score; }
    public String getComment() { return comment; }

    public boolean setScore(int score) {
        if (score >= 1 && score <= 5) {
            this.score = score;
            return true;
        }
        else return false;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Rating) {
            Rating other = (Rating) obj;
            return this.patient == other.patient && this.clinic == other.clinic;
        }
        else return false;
    }
    
    // Returns a string representation of the rating
    @Override
    public String toString() {
        return "Rating{ patient=" + patient + ", clinic=" + clinic + ", score=" + score + ", comment=" + comment + '}';
    }
}

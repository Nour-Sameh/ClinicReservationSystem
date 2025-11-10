package com.mycompany.clinicsystem;

/**
 * Represents a rating given by a Patient to a Clinic.
 * Stores a numerical score (1–5) and an optional comment,
 * linking the patient with the clinic being rated.
 */
public class Rating {    
    private Patient patient; 
    private Clinic clinic;   
    private int score;       // Numerical score (1–5)
    private String comment;  

    // Constructor: creates a new rating with patient, clinic, score, and comment
    Rating(Patient patient, Clinic clinic, int score, String comment){
        this.patient = patient;
        this.clinic = clinic;
        this.score = score;
        this.comment = comment;
    }

    // Returns the patient who provided this rating
    public Patient getPatient() {
        return patient;
    }

    // Returns the clinic that was rated
    public Clinic getClinic() {
        return clinic;
    }

    // Returns the numerical score of this rating
    public int getScore() {
        return score;
    }

    // Returns the comment provided by the patient
    public String getComment() {
        return comment;
    }

    // Sets a new score (must be 1–5)
    public void setScore(int score) {
        if (score >= 1 && score <= 5)
            this.score = score;
        else
            System.out.println("Please score must be between 1 to 5.");
    }

    // Updates the comment for this rating
    public void setComment(String comment) {
        this.comment = comment;
    }

    // Returns a string representation of the rating
    @Override
    public String toString() {
        return "Rating{ patient=" + patient + ", clinic=" + clinic + ", score=" + score + ", comment=" + comment + '}';
    }
}

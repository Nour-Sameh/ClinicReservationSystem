
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.clinicsystem;

/**
 *
 * @author Mariem Tarek
 */
public class Rating {
    private int id;
    private Patient patient;
    private Clinic clinic;
    private int score;
    private String comment;
    
    
    
    // constractor
    Rating(int id , Patient patient , Clinic clinic , int score , String comment){
        this.id=id;
        this.patient=patient;
        this.clinic=clinic;
        this.score=score;
        this.comment=comment;
    }
    
    
    
    //Getter
    public int getId(){
        return id;
    }
    
    public Patient getPatient(){
        return patient;
    }
    
    public Clinic getClinic(){
        return clinic;
    }
    
    public int getScore(){
        return score;
    }
    
    public String getComment(){
        return comment;
    }
    
    
    
    
    //setter
    public void setScore(int score){
        if(score>=1 && score<=5)
            this.score=score;
        else
            System.out.println("please Score must be between 1 to 5.");
    }
    
    
    public void setComment(String comment){
        this.comment=comment;
    }
    
    
    // show comments

    @Override
    public String toString() {
        return "Rating{" + "id=" + id + ", patient=" + patient + ", clinic=" + clinic + ", score=" + score + ", comment=" + comment + '}';
    }
    
}

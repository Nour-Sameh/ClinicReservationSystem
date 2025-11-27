/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.RatingDAO;
import java.sql.SQLException;
import java.util.*;
import model.*;


/**
 *
 * @author mariam
 */
public class RatingService{

    private final RatingDAO ratingDAO = new RatingDAO();

    public boolean addRating(Rating rating) throws SQLException {
        if(isDuplicateRating(rating.getPatient(), rating.getClinic())) {
            return false;
        }
        
        ratingDAO.add(rating);
        rating.getClinic().getRatings().add(rating);
        return true;
    }

    public void deleteRating(Rating rating) throws SQLException {
        ratingDAO.delete(rating.getId());
        rating.getClinic().getRatings().remove(rating);
    }

    public boolean isDuplicateRating(Patient patient, Clinic clinic) throws SQLException {
        List<Rating> ratings = ratingDAO.getAll();
        for(Rating r: ratings) {
            if(r.getPatient().getID() == patient.getID() && r.getClinic().getID() == clinic.getID()) {
                return true;
            }
        }
        return false;
    }
}
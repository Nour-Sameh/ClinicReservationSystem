/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.WaitingListDAO;
import java.sql.SQLException;
import model.WaitingList;

/**
 *
 * @author noursameh
 */

public class WaitingListService {
    
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    
    public void addPatient(WaitingList item) throws SQLException {
        waitingListDAO.add(item);
        item.getClinic().getWaitingList().add(item);
    }

    public void removePatient(WaitingList item) throws SQLException {
        waitingListDAO.delete(item.getId());
        item.getClinic().getWaitingList().remove(item);
    }

}
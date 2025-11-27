/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

/**
 *
 * @author noursameh
 */
import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Connection Failed: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(con);
        }
    }
}

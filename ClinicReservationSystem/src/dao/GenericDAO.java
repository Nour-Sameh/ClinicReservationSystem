/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;
import java.util.List;
import java.sql.SQLException;
/**
 *
 * @author noursameh
 */
public interface GenericDAO<T> {
    
    void add(T entity) throws SQLException;

    T getById(int id) throws SQLException;

    List<T> getAll() throws SQLException;

    void update(T entity) throws SQLException;

    void delete(int id) throws SQLException;
    
}

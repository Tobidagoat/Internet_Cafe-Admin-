/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package internet_cafe_admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
/**
 *
 * @author USER
 */
public class databaseinsert {
    
    public static void main(String []args){
        int[] generalRoomIds = {1, 2}; 
        String username="root";
        String password=".";
        String url="jdbc:mysql://localhost:3306/internet_cafe";
try (Connection conn = DriverManager.getConnection(url, username, password)) {
    String insertSQL = "INSERT INTO pcs (room_id, pc_no) VALUES (?, ?)";
    PreparedStatement stmt = conn.prepareStatement(insertSQL);

    for (int roomId : generalRoomIds) {
        for (int i = 1; i <= 50; i++) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, i);
            stmt.addBatch();
        }
    }

    stmt.executeBatch();
    System.out.println("Done!");
} catch (SQLException e) {
    e.printStackTrace();
}

    }
    
}

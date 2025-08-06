/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author MITUSER-1
 */
public class DbConnection {
    
    public Connection getConnection() throws ClassNotFoundException{
        Connection con=null;

//         String username = "User";
//    String password = "12345"; // or your actual MySQL password
//    String url = "jdbc:mysql://172.16.201.209:3306/internet_cafe?useSSL=false&serverTimezone=UTC";

         String username = "root";
    String password = "20051120"; // or your actual MySQL password
    String url = "jdbc:mysql://localhost:3306/internet_cafe?useSSL=false&serverTimezone=UTC";

        
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Load Driver.......");
        
        try{
        con =DriverManager.getConnection(url, username, password);
        System.out.println("database connected");
        }catch(SQLException ex){
            System.out.println("not connect");
    }
    return con;
}
}
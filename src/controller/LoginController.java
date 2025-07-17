/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import internet_cafe_admin.Internet_Cafe_admin;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class LoginController implements Initializable {

    @FXML
    private TextField txtusername;
    @FXML
    private Button btnclose;
    @FXML
    private PasswordField txtpassword;
    @FXML
    private Button btnlogin;
    @FXML
    private Button signinbtn;
    @FXML
    private Label lbusernameerror;
    @FXML
    private Label lbpasserror;
    
    Parent root;
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    Stage stage=Internet_Cafe_admin.stage;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DbConnection db=new DbConnection();
        try {
            con=db.getConnection();
        } catch (ClassNotFoundException ex) {
            
        }
    }    

    @FXML
    private void loginaction(ActionEvent event) throws SQLException, IOException {
        boolean valid=false;
        
        String username=txtusername.getText();
        String password=txtpassword.getText();
        
        if(txtusername.getText().isEmpty()){
            lbusernameerror.setText("Please enter username");
           
        }
        else{
            lbusernameerror.setText("");
            
        }    
        if(txtpassword.getText().isEmpty()){
            lbpasserror.setText("Please enter password");
         }
        else{
            lbpasserror.setText("");
        }
        if(!txtusername.getText().isEmpty() && !txtpassword.getText().isEmpty()){
            valid=true;
        }
           
        
        if(valid){
            String sql="Select * from admins where admin_name=? and password=?";
            pst=con.prepareStatement(sql);
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            rs=pst.executeQuery();

//            Stage stage=Internet_Cafe_admin.stage;
           if(rs.next()){
               root = FXMLLoader.load(getClass().getResource("/view/Default.fxml"));

            Scene scene = new Scene(root);
//            stage.initStyle(StageStyle.UTILITY);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            
            stage.show();
               
           }else{
               JOptionPane.showMessageDialog(null, "Username or Password  is wrong!");
           }
            
            
        }
    }

    @FXML
    private void signinaction(ActionEvent event) throws IOException {
        
        
    }
    
    @FXML
    void closeaction(ActionEvent event) {
        stage.close();
    }
    
    @FXML
    void usertxt(ActionEvent event) {
        txtpassword.requestFocus();
    }
    @FXML
    void passwordtxt(ActionEvent event) {
        btnlogin.fire();
    }
    
}

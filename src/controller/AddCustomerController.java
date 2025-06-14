/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import internet_cafe_admin.Internet_Cafe_admin;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;


/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class AddCustomerController implements Initializable {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnAdd;
    
     Statement st;
    PreparedStatement pst;
    
    Connection con = null;
    private Runnable onCustomerAdded;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        DbConnection db = new DbConnection();
        
        try {
            con = db.getConnection();
            // TODO
        } catch (ClassNotFoundException ex) {
           
        }
    }    

    @FXML
    private void HandleNameAction(ActionEvent event) {
        txtPhone.requestFocus();
    }

    @FXML
    private void HandlePhoneAction(ActionEvent event) {
        txtEmail.requestFocus();

    }

    @FXML
    private void HandleEmailAction(ActionEvent event) {
    }

    @FXML
    private void HandleCancelAction(ActionEvent event) {
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
    }

    @FXML
    private void HandleAddAction(ActionEvent event) throws SQLException, IOException {
        
        
        if(txtName.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Pls Fill the Name!");
        }else if(txtPhone.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Pls Fill the Phone Number!");
        }else{
            
             LocalDate date = LocalDate.now();
            String day = date.toString();
            
            
            String sql = "insert into user (customer_name,ph_no,e_mail,date) values(?,?,?,?)";
            pst = con.prepareStatement(sql);
            pst.setString(1, txtName.getText().trim());
            pst.setString(2, txtPhone.getText().trim());
            pst.setString(3, txtEmail.getText().trim());
            pst.setString(4, day);
            
           
           
            
            
           pst.executeUpdate();
           
            if(onCustomerAdded !=null){
                onCustomerAdded.run();
            }
           Stage stage = (Stage)btnAdd.getScene().getWindow();
           stage.close();
           
           
           
            
            
            
        }
    }
    public void setOnCustomerAdded(Runnable onCustomerAdded){
        this.onCustomerAdded = onCustomerAdded;
    }
    
}

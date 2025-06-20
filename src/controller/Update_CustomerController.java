/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import model.customer;

/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class Update_CustomerController implements Initializable {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnUpdate;
    
    ObservableList<customer> customerList;
      Statement st;
    PreparedStatement pst;
    private Runnable onCustomerAdded1;
    
    Connection con = null;
    int ccId;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        DbConnection db = new DbConnection();
        try {
            con = db.getConnection();
            
            
            
            

            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Update_CustomerController.class.getName()).log(Level.SEVERE, null, ex);
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
    private void HandleUpdateAction(ActionEvent event) throws SQLException {
        
                if(txtName.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Please Fill the Name!");
        }
        else if(txtPhone.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Plsease Fill the Phone Number!");    
            
        }
        else{
            if(!txtPhone.getText().startsWith("09")){
                JOptionPane.showMessageDialog(null, "Phone Number is not Eligible!");  
            }
            else if(txtPhone.getText().length()!=11){
                 JOptionPane.showMessageDialog(null, "Phone Number is not Eligible!");  
            }else{
                
                if(!txtEmail.getText().isEmpty()){
                    if(!txtEmail.getText().endsWith("@gmail.com")){
                        JOptionPane.showMessageDialog(null, "E-mail is not Eligible!");
                    }
                    else{
                         
            
            
                          String sql = "update users set customer_name =?,ph_no =?, e_mail=? where customer_id =?";
                          pst = con.prepareStatement(sql);
                          pst.setString(1, txtName.getText().trim());
                          pst.setString(2, txtPhone.getText().trim());
                          pst.setString(3, txtEmail.getText().trim());
                          pst.setInt(4, ccId);
                          
            
           
           
                          pst.executeUpdate();
           
                          if(onCustomerAdded1 !=null){
                            onCustomerAdded1.run();
                            }
                          Stage stage = (Stage)btnUpdate.getScene().getWindow();
                          stage.close();
           
                        
                    }
        }else{
                     String sql = "update users set customer_name =?,ph_no =?, e_mail=? where customer_id =?";
                          pst = con.prepareStatement(sql);
                          pst.setString(1, txtName.getText().trim());
                          pst.setString(2, txtPhone.getText().trim());
                          pst.setString(3, txtEmail.getText().trim());
                          pst.setInt(4, ccId);
                          
            
           
           
                          pst.executeUpdate();
           
                          if(onCustomerAdded1 !=null){
                            onCustomerAdded1.run();
                            }
                          Stage stage = (Stage)btnUpdate.getScene().getWindow();
                          stage.close();

                }
                
                
            }
        
        
    }
    }
    public void UpdateData (int id,String name, String phno, String email){
        
        this.ccId = id;
        txtName.setText(name);
        txtPhone.setText(phno);
        txtEmail.setText(email);
    }
     public void setOnCustomerAdded1(Runnable onCustomerAdded1){
        this.onCustomerAdded1 = onCustomerAdded1;
    }
    

}

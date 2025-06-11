/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import internet_cafe_admin.Internet_Cafe_admin;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

    
/**
 * FXML Controller class
 *
 * @author USER
 */
public class SignupController implements Initializable {

    @FXML
    private TextField txtusername;
    @FXML
    private PasswordField txtpassword;
    @FXML
    private Button btnsignup;
    @FXML
    private Button signinbtn;
    @FXML
    private Button btnclose;
    @FXML
    private Button loginbtn;
    
    
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
        // TODO
    }    

    @FXML
    private void signupaction(ActionEvent event) throws IOException {
       
    }

    @FXML
    private void loginaction(ActionEvent event) throws IOException {
        
        
    }

    @FXML
    private void closeaction(ActionEvent event) {
        stage.close();
    }
    
}

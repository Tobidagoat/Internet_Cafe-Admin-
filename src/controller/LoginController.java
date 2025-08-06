/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import internet_cafe_admin.Internet_Cafe_admin;
import internet_cafe_admin.server;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javafx.application.Platform;
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
    private Button btnsignup;
    @FXML
    private Label lbusernameerror;
    @FXML
    private Label lbpasserror;
    
    Parent root;
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    Stage stage=Internet_Cafe_admin.stage;
    private String username;
    private String password;
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
         Platform.runLater(() -> txtusername.requestFocus());
    }    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    @FXML
    private void loginaction(ActionEvent event) throws SQLException, IOException {
        boolean valid=false;
        
        username=txtusername.getText();
        password=txtpassword.getText();
        
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
           
        
        if(valid) {
            String hashedPassword = hashPassword(password);
            String sql = "SELECT * FROM admins WHERE admin_name = ? AND password = ?";
            pst = con.prepareStatement(sql);
            
            pst.setString(1, username);
            pst.setString(2, hashedPassword); // Compare hashed passwords
            
            rs = pst.executeQuery();

            if (rs.next()) {
                String adminpf = rs.getString("admin_profile_pic");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Default.fxml"));
                Parent root = loader.load();
                DefaultController controller = loader.getController();
                controller.getadmininfo(username, adminpf);

                Internet_Cafe_admin.stage.setScene(new Scene(root));
                Internet_Cafe_admin.stage.centerOnScreen();
                Internet_Cafe_admin.stage.setMaximized(true);
            } else {
                JOptionPane.showMessageDialog(null, "Username or Password is wrong!");
            }
        }
    }

    @FXML
    private void btnsignupaction(ActionEvent event) throws IOException {
    // Hide the current login window
    Internet_Cafe_admin.stage.hide();
    
    // Load signup
    Parent root = FXMLLoader.load(getClass().getResource("/view/signup.fxml"));
    Stage signupStage = new Stage();
    signupStage.initOwner(Internet_Cafe_admin.stage);
    signupStage.initStyle(StageStyle.DECORATED);
    signupStage.setScene(new Scene(root));
    signupStage.setTitle("Sign Up");
    
    // When signup closes, show login again
//    signupStage.setOnHidden(e -> {
//        Internet_Cafe_admin.stage.show();
//    });
    
    signupStage.show();
    }
    
    @FXML
    void closeaction(ActionEvent event) {
        stage.close();
        server.getInstance().stopServer();
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

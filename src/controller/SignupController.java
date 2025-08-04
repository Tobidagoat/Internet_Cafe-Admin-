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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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
    
    Parent root;
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    Stage stage=Internet_Cafe_admin.stage;
    @FXML
    private MediaView videobg;
    @FXML
    private AnchorPane signuppane;
    @FXML
    private TextField txtnewemail;
    @FXML
    private Button btnsignin;
    @FXML
    private TextField txtnewusername;
    @FXML
    private PasswordField txtnewpassword;
    @FXML
    private AnchorPane loginpane;
    @FXML
    private Button btnconfirm;
    
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        signuppane.setVisible(false);
        loginpane.setVisible(true);
        DbConnection db=new DbConnection();
        try {
            con = db.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Background video setup (uncomment if you have the video file)
        /*
        String videoPath = getClass().getResource("/img/login-bg-video.mp4").toExternalForm();
        Media media = new Media(videoPath);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setAutoPlay(true);
        videobg.setMediaPlayer(mediaPlayer);
        */
    }

@FXML
private void btnsignupaction(ActionEvent event) {
    String email = txtnewemail.getText().trim();
    String username = txtnewusername.getText().trim();
    String password = txtnewpassword.getText().trim();
    
    if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
        showAlert("Input Error", "Please fill in all fields");
        return;
    }
    
    if (!isValidEmail(email)) {
        showAlert("Input Error", "Please enter a valid email address");
        return;
    }
    
    if (password.length() < 6) {
        showAlert("Input Error", "Password must be at least 6 characters");
        return;
    }
    
    try {
        String checkSql = "SELECT * FROM admins WHERE admin_name = ? OR email = ?";
        pst = con.prepareStatement(checkSql);
        pst.setString(1, username);
        pst.setString(2, email);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            showAlert("Registration Error", "Username or email already exists");
            return;
        }
        
        // Insert new user
        String insertSql = "INSERT INTO admins (admin_name, email, password) VALUES (?, ?, ?)";
        pst = con.prepareStatement(insertSql);
        pst.setString(1, username);
        pst.setString(2, email);
        pst.setString(3, hashPassword(password));
        int rowsAffected = pst.executeUpdate();
        
        if (rowsAffected > 0) {
            showAlert("Success", "New Admin registered successfully!");
            txtnewemail.clear();
            txtnewusername.clear();
            txtnewpassword.clear();
            
            // Close the signup window
            Stage signupStage = (Stage) btnsignup.getScene().getWindow();
            signupStage.close();
            Internet_Cafe_admin.stage.setMaximized(false);
            Internet_Cafe_admin.stage.show();
        } else {
            showAlert("Registration Error", "Failed to register user");
        }
    } catch (SQLException ex) {
        showAlert("Error", "An error occurred: " + ex.getMessage());
        Logger.getLogger(SignupController.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    @FXML
    private void btnsigninaction(ActionEvent event) throws IOException {
    // Close current signup window
    Stage currentStage = (Stage) btnsignin.getScene().getWindow();
    currentStage.close();
    
    // Show the login window (static stage)
    Internet_Cafe_admin.stage.setMaximized(false);
    Internet_Cafe_admin.stage.show();
    }

    @FXML
    private void btnconfirmaction(ActionEvent event) {
        
        String username = txtusername.getText().trim();
        String password = txtpassword.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password");
            return;
        }
        
        try {
            // Check if the credentials match an admin account
            String sql = "SELECT * FROM admins WHERE admin_name = ? AND password = ?";
            pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, hashPassword(password));
            
            rs = pst.executeQuery();
            
            if (rs.next()) {
                showAlert("Correct!", "Information are correct!");
                //fade animation
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginpane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    loginpane.setVisible(false);
                    signuppane.setOpacity(0);
                    signuppane.setVisible(true);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), signuppane);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                showAlert("Authentication Failed", "Invalid admin username or password");
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error verifying admin: " + ex.getMessage());
        }
        //gg

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
    private void btncloseaction(MouseEvent event) {
        // Get stage from the event source (button)
    Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
    stage.close();
        server.getInstance().stopServer();
    }
    
    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Close database connection when controller is disposed
    public void closeConnection() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (SQLException ex) {
            System.err.println("Error closing database resources: " + ex.getMessage());
        }
    }
    @FXML
    void txtnewemail(ActionEvent event) {
        txtnewpassword.requestFocus();
    }

    @FXML
    void txtnewpass(ActionEvent event) {
        btnsignup.fire();
    }

    @FXML
    void txtnewuser(ActionEvent event) {
        txtnewemail.requestFocus();
    }

    @FXML
    void txtpass(ActionEvent event) {
        btnconfirm.fire();
    }

    @FXML
    void txtuser(ActionEvent event) {
        txtpassword.requestFocus();
    }
}
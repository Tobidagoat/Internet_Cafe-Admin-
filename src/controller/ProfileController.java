package controller;

import database.DbConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProfileController implements Initializable {

    @FXML private TextField txtname;
    @FXML private TextField txtemail;
    @FXML private TextField txtoldpassword;
    @FXML private TextField txtnewpassword;
    @FXML private Button btncancel;
    @FXML private Button btnsave;
    @FXML private ImageView profileImage;
    
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    private File selectedImageFile;
    private String originalHashedPassword;
    private String originalEmail;
    private String originalpic;
    private String name;
    private boolean componentsInitialized = false;
    private Stage currentStage;
    private ProfileUpdateCallback updateCallback;
    
    public interface ProfileUpdateCallback {
        void onProfileUpdated(String newName, String newProfileImage);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[DEBUG] initialize() started");

        
        try {
            con = new DbConnection().getConnection();
            System.out.println("[DEBUG] Database connection: " + (con != null ? "SUCCESS" : "FAILED"));
            
            if (profileImage == null || txtname == null || txtemail == null) {
                System.err.println("[ERROR] FXML injection failed! Check FXML file.");
            } else {
                componentsInitialized = true;
                System.out.println("[DEBUG] FXML components loaded successfully");
                loadDefaultImage();
                
                // Prevent auto-focus on txtname
                Platform.runLater(() -> txtname.getParent().requestFocus());
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR] Initialize failed: " + ex.getMessage());
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    
    public void getname(String name) {
        System.out.println("[DEBUG] getname() called with: " + name);
        this.name = name;
        
        if (componentsInitialized) {
            try {
                loadOriginalData(name);
            } catch (SQLException ex) {
                System.err.println("[ERROR] Failed to load data: " + ex.getMessage());
                showAlert("Error", "Failed to load profile data");
            }
        } else {
            System.out.println("[WARN] Components not ready, deferring load...");
            Platform.runLater(() -> {
                try {
                    loadOriginalData(name);
                } catch (SQLException ex) {
                    System.err.println("[ERROR] Deferred load failed: " + ex.getMessage());
                }
            });
        }
    }
    
    public void setProfileUpdateCallback(ProfileUpdateCallback callback) {
        this.updateCallback = callback;
}

    private void loadOriginalData(String adminname) throws SQLException {
        System.out.println("[DEBUG] Loading data for: " + adminname);
        
        String sql = "SELECT email, admin_profile_pic, password FROM admins WHERE admin_name = ?";
        stmt = con.prepareStatement(sql);
        stmt.setString(1, adminname);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            originalEmail = rs.getString("email");
            originalpic = rs.getString("admin_profile_pic");
            originalHashedPassword = rs.getString("password");
            
            System.out.println("[DEBUG] Retrieved: Email=" + originalEmail 
                + ", Image=" + originalpic 
                + ", Password=" + (originalHashedPassword != null ? "[HASHED]" : "null"));
            
            Platform.runLater(() -> {
                txtemail.setText(originalEmail);
                txtname.setText(adminname);
                
                if (originalpic != null && !originalpic.trim().isEmpty()) {
                    File imageFile = new File("src/img/" + originalpic);
                    System.out.println("[DEBUG] Image path: " + imageFile.getAbsolutePath());
                    
                if (imageFile.exists()) {
                    Image image = new Image(
                        imageFile.toURI().toString(),
                        550,  
                        550,  
                        true, 
                        true, 
                        true  
                    );
                    profileImage.setImage(image);
                    profileImage.setPreserveRatio(true);
                    profileImage.setSmooth(true);
                    profileImage.setCache(true);
                } else {
                    loadDefaultImage();
                }
                } else {
                    loadDefaultImage();
                }
                makeImageCircular();
            });
        } else {
            System.err.println("[ERROR] No admin found with name: " + adminname);
            showAlert("Error", "Profile data not found");
        }
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.jpg"));
            profileImage.setImage(defaultImage);
            profileImage.setPreserveRatio(true);
            profileImage.setSmooth(true);
            profileImage.setCache(true);
            System.out.println("[DEBUG] Default image loaded");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load default image: " + e.getMessage());
        }
    }

    private void makeImageCircular() {
        if (profileImage.getImage() != null) {
            double radius = Math.min(profileImage.getFitWidth(), profileImage.getFitHeight()) / 2;
            profileImage.setClip(new Circle(radius, radius, radius));
        }
    }

    @FXML
    private void changephoto(MouseEvent event) {
        if (!componentsInitialized || profileImage == null) {
            showAlert("Error", "Profile image not initialized");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Window window = profileImage.getScene() != null ? 
            profileImage.getScene().getWindow() : 
            ((javafx.scene.Node)event.getSource()).getScene().getWindow();
        
        selectedImageFile = fileChooser.showOpenDialog(window);
        
        if (selectedImageFile != null) {
            try {
                Image image = new Image(
                    selectedImageFile.toURI().toString(),
                        550,  
                        550,  
                        true, 
                        true, 
                        true  
                    );
                    profileImage.setImage(image);
                    profileImage.setPreserveRatio(true);
                    profileImage.setSmooth(true);
                    profileImage.setCache(true);
                makeImageCircular();
            } catch (Exception e) {
                showAlert("Error", "Failed to load selected image");
            }
        }
    }

    @FXML
    private void btncancelaction(ActionEvent event) throws SQLException {
        txtemail.setText(originalEmail);
        txtoldpassword.clear();
        txtnewpassword.clear();
        if (originalpic != null) {
            loadOriginalData(name);
        }
            currentStage=(Stage) btnsave.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close(); // Close window on successful save
            }
    }

    @FXML
    private void btnsaveaction(ActionEvent event) {
        // Email validation
        if (!txtemail.getText().endsWith("@gmail.com")) {
            showAlert("Error", "Email must end with @gmail.com");
            return;
        }

        if (!txtnewpassword.getText().isEmpty()) {
            String enteredOldHash = hashPassword(txtoldpassword.getText());
            if (!enteredOldHash.equals(originalHashedPassword)) {
                showAlert("Error", "Old password is incorrect");
                return;
            }
        }

        if (updateProfile()) {
            if (updateCallback != null) {
                updateCallback.onProfileUpdated(txtname.getText(), originalpic);
            }
            currentStage = (Stage) btnsave.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        }
    }

    private boolean updateProfile() {
        try {
            String sql = "UPDATE admins SET admin_name = ?, email = ?" +
                (txtnewpassword.getText().isEmpty() ? "" : ", password = ?") +
                " WHERE admin_name = ?";

            stmt = con.prepareStatement(sql);
            int paramIndex = 1;

            stmt.setString(paramIndex++, txtname.getText());
            stmt.setString(paramIndex++, txtemail.getText());

            if (!txtnewpassword.getText().isEmpty()) {
                stmt.setString(paramIndex++, hashPassword(txtnewpassword.getText()));
            }

            stmt.setString(paramIndex, name);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Profile updated successfully");
                name = txtname.getText(); 
                if (!txtnewpassword.getText().isEmpty()) {
                    originalHashedPassword = hashPassword(txtnewpassword.getText());
                }
                
                if (selectedImageFile != null) {
                    saveProfileImage();
                }
                return true;
            } else {
                showAlert("Error", "Failed to update profile");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Update failed: " + e.getMessage());
        }
        return false;
    }

    private void saveProfileImage() {
        try {
            new File("src/img").mkdirs();
            String newFilename = "profile_" + name + "_" + System.currentTimeMillis() + 
                selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
            
            File destination = new File("src/img/" + newFilename);
            Files.copy(selectedImageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            String updateSql = "UPDATE admins SET admin_profile_pic = ? WHERE admin_name = ?";
            stmt = con.prepareStatement(updateSql);
            stmt.setString(1, newFilename);
            stmt.setString(2, name);
            stmt.executeUpdate();
            
            originalpic = newFilename;
        } catch (IOException | SQLException e) {
            showAlert("Error", "Failed to save profile image");
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
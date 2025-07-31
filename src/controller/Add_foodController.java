/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author MITUSER-2
 */

import database.DbConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.foods;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Add_foodController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField imagePathField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button uploadBtn;
    @FXML private ComboBox<String> foodTypeComboBox;

    private boolean editMode = false;
    private foods food;
    private foods updatedFood = null;

    

    public void setFood(foods food) {
        this.food = food;
        if (food != null) {
            nameField.setText(food.getName());
            priceField.setText(food.getPrice().toString());
            stockField.setText(String.valueOf(food.getStock()));
            imagePathField.setText(food.getImagePath());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFoodTypes();

        saveBtn.setOnAction(e -> saveFood());
        cancelBtn.setOnAction(e -> closeWindow());
        uploadBtn.setOnAction(e -> handleImageUpload());

        foodTypeComboBox.setOnAction(event -> {
            String selectedType = foodTypeComboBox.getValue();
            System.out.println("Selected food type: " + selectedType);
        });
    }
    
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    private void loadFoodTypes() {
        ObservableList<String> foodTypes = FXCollections.observableArrayList("snack", "meal", "drink", "dessert");
        Platform.runLater(() -> {
            foodTypeComboBox.setItems(foodTypes);
            if (food != null && food.getFoodType() != null && foodTypes.contains(food.getFoodType())) {
                foodTypeComboBox.setValue(food.getFoodType());
            }
        });
    }

    @FXML
    public void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage currentStage = (Stage) uploadBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getName());
        }
    }

    private void saveFood() {
        String name = nameField.getText().trim();
        String foodType = foodTypeComboBox.getValue();
        String stockText = stockField.getText().trim();
        Double price = Double.parseDouble(priceField.getText().trim());
        String imagePath = imagePathField.getText().trim();

        if (name.isEmpty() || foodType == null || price == null || stockText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all fields.");
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock must be a number.");
            return;
        }

        DbConnection db = new DbConnection();

        try (Connection con = db.getConnection()) {
            if (editMode && food != null) {
                String sql = "UPDATE foods SET food_name=?, food_type=?, stock=?, price=?, img=? WHERE food_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, foodType);
                ps.setInt(3, stock);
                ps.setDouble(4, price);
                ps.setString(5, imagePath);
                ps.setInt(6, food.getId());
                ps.executeUpdate();
                
                updatedFood = new foods(food.getId(), name, foodType, stock, price, imagePath);
            } else {
                String sql = "INSERT INTO foods(food_name, food_type, stock, price, img) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setString(2, foodType);
                ps.setInt(3, stock);
                ps.setDouble(4, price);
                ps.setString(5, imagePath);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    updatedFood = new foods(id, name, foodType, stock, price, imagePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save food.");
        }

        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public foods getUpdatedFood() {
        return updatedFood;
    }
}

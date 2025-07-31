/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

//import java.net.URL;
//import java.util.ResourceBundle;
import database.DbConnection;
import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import java.io.File;
import javafx.scene.image.Image;
import model.foods;
import java.net.URL;
import java.sql.Connection;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.PreparedStatement;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author MITUSER-2
 */


public class Food_cardController {
    
    @FXML private AnchorPane cardRoot;
    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Button editBtn;
    @FXML private Label foodTypeLabel;
    private foods currentFood;
    private FoodController foodController;
;

    public void setData(foods food) {
        this.currentFood = food;
        nameLabel.setText(food.getName());
        priceLabel.setText("MMK " + food.getPrice());
        foodTypeLabel.setText(food.getFoodType());
        System.out.println("food card controller is working");
        if (food.getImage() != null) {
            File file = new File(food.getImage());
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
            }
        }

           try {
            String imagePath = food.getImagePath();
            System.out.println("Path from DB (food.getImagePath()): " + imagePath); 
             if (imagePath != null && !imagePath.isEmpty()) {
            URL imageUrl = getClass().getResource("/img/" + imagePath); 

            System.out.println("Attempting to load classpath URL: " + "/" + imagePath); 
            if (imageUrl != null) {
                System.out.println("Image found at URL: " + imageUrl.toExternalForm()); 
                imageView.setImage(new Image(imageUrl.toExternalForm(), true));
            } else {
                System.err.println("Image resource NOT FOUND on classpath: " + imagePath);
                imageView.setImage(null);
            }
        } else {
            imageView.setImage(null);
        }
    } catch (Exception e) {
        System.err.println("Error loading image for " + food.getName() + " from path: " + food.getImagePath());
        e.printStackTrace();
        imageView.setImage(null);
    }
    }
    
    public void setFoodController(FoodController controller) {
        this.foodController = controller;
    }
    

//    public void setFoodController(FoodController controller) {
//        this.foodController = controller;
//        editBtn.setOnAction(e -> {
//            foodController.openForm(food);
//        });
//    }
    

    @FXML
    void onEdit(ActionEvent event) {
        if (foodController != null && currentFood != null) {
        foodController.openForm(currentFood); 
        System.out.println("edit btn triggered");
    } else {
        System.err.println("Edit failed: controller or food is null.");
    }
    }
    


//    @FXML
//public void onDelete() {
//    if (currentFood == null) {
//        System.err.println("currentFood is null. Cannot delete.");
//        return;
//    }
//
//    int foodId = currentFood.getId();
//
//    // Call DB delete logic here
//    DbConnection db = new DbConnection();
//    try (Connection conn = db.getConnection()) {
//        String sql = "DELETE FROM foods WHERE food_id = ?";
//        PreparedStatement stmt = conn.prepareStatement(sql);
//        stmt.setInt(1, currentFood.getId());
//         stmt.executeUpdate();;
//        
//        if (cardRoot.getParent() instanceof javafx.scene.layout.Pane) {
//                ((javafx.scene.layout.Pane) cardRoot.getParent()).getChildren().remove(cardRoot);
//            }
//
//        // Optionally remove the card from the UI // If inside a VBox
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}

    
}

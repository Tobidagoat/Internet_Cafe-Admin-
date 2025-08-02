package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.foods;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class FoodController implements Initializable {
    
    @FXML private FlowPane cardContainer;
    @FXML private Button btnNew;
    @FXML private AnchorPane foodviewpane;
    @FXML private AnchorPane foodorderpane;
    @FXML private Button food;
    @FXML private Button food_order;
    @FXML private TextField txtfoodsearch;
    @FXML private HBox ordercardscontainer;
    
    private List<foods> foodList = new ArrayList<>();
    private List<foods> filteredList = new ArrayList<>();
    private boolean isFiltered = false;
    
    // Static list to maintain order data across instances
    private static final List<OrderData> orderDataList = new ArrayList<>();
    
    // Static instance reference
    private static FoodController instance;
    
    // Inner class to store order data
    private static class OrderData {
        String clientName;
        ArrayList<foods> cartList;
        
        public OrderData(String clientName, ArrayList<foods> cartList) {
            this.clientName = clientName;
            this.cartList = cartList;
        }
        
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        loadFromDatabase();
        showFoodCards();
        setupUI();
        server.getInstance().setFoodController(this);
        try {
            reloadOrderCards(); // Load any existing orders
        } catch (SQLException ex) {
            Logger.getLogger(FoodController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static FoodController getInstance() {
        return instance;
    }

    private void setupUI() {
        foodviewpane.setVisible(true);
        foodorderpane.setVisible(false);
        
        food.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
        food_order.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
        
        txtfoodsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchFoods(newValue);
        });
    }
    
    private void loadFromDatabase() {
        foodList.clear();
        String query = "SELECT food_id, food_name, food_type, stock, price, img FROM foods";
        DbConnection db = new DbConnection();
        try (Connection con = db.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                foodList.add(new foods(
                    rs.getInt("food_id"),
                    rs.getString("food_name"),
                    rs.getString("food_type"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("img")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showFoodCards() {
        cardContainer.getChildren().clear();
        isFiltered = false;
        displayFoodCards(foodList);
    }
    
    private void searchFoods(String searchText) {
        filteredList.clear();
        
        if (searchText == null || searchText.isEmpty()) {
            showFoodCards();
            return;
        }
        
        String searchLower = searchText.toLowerCase();
        for (foods food : foodList) {
            if (food.getName().toLowerCase().contains(searchLower)) {
                filteredList.add(food);
            }
        }
        
        isFiltered = true;
        displayFoodCards(filteredList);
    }
    
    private void displayFoodCards(List<foods> foodsToDisplay) {
        cardContainer.getChildren().clear();
        
        for (foods food : foodsToDisplay) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/food_card.fxml"));
                AnchorPane card = loader.load();
                
                Food_cardController cardController = loader.getController();
                cardController.setData(food);
                cardController.setFoodController(this);
                
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void openForm(foods foodToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_food.fxml"));
            Parent root = loader.load();

            Add_foodController controller = loader.getController();
            if (foodToEdit != null) {
                controller.setEditMode(true);
                controller.setFood(foodToEdit);
            } else {
                controller.setEditMode(false);
            }

            Stage stage = new Stage();
            stage.setTitle(foodToEdit == null ? "Add New Food" : "Edit Food");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            foods updatedFood = controller.getUpdatedFood();
            if (updatedFood != null) {
                updateFoodList(foodToEdit, updatedFood);
                refreshView();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFoodList(foods oldFood, foods newFood) {
        if (oldFood == null) {
            foodList.add(newFood);
        } else {
            int index = foodList.indexOf(oldFood);
            if (index != -1) {
                foodList.set(index, newFood);
            }
        }
    }
    
    private void refreshView() {
        if (isFiltered) {
            searchFoods(txtfoodsearch.getText());
        } else {
            showFoodCards();
        }
    }
    
    @FXML
    private void handleAddButtonClick(ActionEvent event) {
        openForm(null);
    }

    @FXML
    void foodAction(ActionEvent event) {
        food.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
        food_order.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
        foodviewpane.setVisible(true);
        foodorderpane.setVisible(false);
    }

    @FXML
    void handleOrderAction(ActionEvent event) throws SQLException {
        food_order.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
        food.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
        foodorderpane.setVisible(true);
        foodviewpane.setVisible(false);
        reloadOrderCards(); 
    }

    @FXML
    private void handlefoodsearch(ActionEvent event) {
        searchFoods(txtfoodsearch.getText());
    }


    public void addOrderCard(String clientName, ArrayList<foods> cartList) throws SQLException {
        orderDataList.add(new OrderData(clientName, cartList));
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/foodordercard.fxml"));
            AnchorPane orderCard = loader.load();
            
            FoodordercardController cardController = loader.getController();
            cardController.setOrderDetails(clientName, cartList);
            cardController.setFoodController(this);
            
            orderCard.getProperties().put("controller", cardController);
            ordercardscontainer.getChildren().add(orderCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void removeOrderCard(Node card) {
        Object controllerObj = card.getProperties().get("controller");

        if (controllerObj instanceof FoodordercardController controller) {
            orderDataList.removeIf(data -> 
                data.clientName.equals(controller.getClientName())
            );
        }
        ordercardscontainer.getChildren().remove(card);
    }

        private void reloadOrderCards() throws SQLException {
        ordercardscontainer.getChildren().clear();
        
        for (OrderData data : orderDataList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/foodordercard.fxml"));
                AnchorPane orderCard = loader.load();
                
                FoodordercardController cardController = loader.getController();
                cardController.setOrderDetails(data.clientName, data.cartList);
                cardController.setFoodController(this);
                
                // Store controller reference in the card for later removal
                orderCard.getProperties().put("controller", cardController);
                
                ordercardscontainer.getChildren().add(orderCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
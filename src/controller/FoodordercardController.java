/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import model.foods;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class FoodordercardController implements Initializable {

    @FXML
    private Button btnconfirm;
    @FXML
    private Label lbname;
    @FXML
    private ImageView userpfp;
    @FXML
    private Label lbtimestamp;
    @FXML
    private Label lbtotalamount;
    @FXML
    private Button btncancel;
    @FXML
    private FlowPane fooditem_container;
    
    DbConnection db=new DbConnection();
    Connection con;
    private int pcid;
    private int saleid;
    private int orderid=0;
    private String clientname;
    private int no;
    ArrayList<foods> cartList;
    private FoodController foodController;
    @FXML
    private Label lbroomno;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            con = db.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FoodordercardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public void setFoodController(FoodController foodController) {
        this.foodController = foodController;
    }
        

    private Node getCardNode(ActionEvent event) {
        Node node = (Node) event.getSource();
        while (node != null && !(node instanceof AnchorPane)) {
            node = node.getParent();
        }
        return node;
    }
    
    @FXML
    private void btnconfirmaction(ActionEvent event) throws SQLException {
        Node orderCard = getCardNode(event);
        no=isexist(pcid);
        if (no == 0) {
            String sql = "Insert into food_order(sale_id, sale_date, total_food_price) values(?, Current_date, null)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, getsaleid(pcid));
                stmt.executeUpdate();
            }
        }
        
        double total = 0;
        String detailsql = "Insert into food_order_detail (order_id, food_id, qty) values (?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(detailsql);
        for(foods item : cartList) {
            int foodid = getFoodId(item.getName());
            if(foodid == -1) {
                throw new SQLException("Food item not found: " + item.getName());
            }
            stmt.setInt(1, getorderid(getsaleid(pcid)));
            stmt.setInt(2, foodid);
            stmt.setInt(3, item.getQuantity());
            stmt.addBatch();
            total += item.getPrice() * item.getQuantity();
        }
        stmt.executeBatch();
        
        String msg = "Food Order Request Accepted.";
        server.sendToClient("TO|" + clientname + "|CONFIRMATION|" + msg);
        
        foodController.removeOrderCard(orderCard);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Order Confirmed");
        alert.setContentText("Order ID: " + getorderid(getsaleid(pcid)));
        alert.show();
        
        
    }
    
    private int isexist(int pcid) throws SQLException{
        String sql="select count(sale_id) as no from food_order where sale_id = ?"; 
        PreparedStatement stmt=con.prepareStatement(sql);
        stmt.setInt(1, getsaleid(pcid));
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            no=rs.getInt("no");
        }
        return no;
    }
    
    @FXML
    private void btncancelaction(ActionEvent event) {
        Node orderCard = getCardNode(event);
        
        foodController.removeOrderCard(orderCard);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cancelled");
        alert.setHeaderText("Order Cancelled");
        alert.show();
         String msg = "Food Order Request Declined.";
        server.sendToClient("TO|" + clientname + "|CONFIRMATION|" + msg);
    }

    
    private int getsaleid(int pcid) throws SQLException{
        int sale=0;
        String sql="Select sale_id from sale_detail where pc_id = ? and status_id = 2";
        PreparedStatement stmt=con.prepareStatement(sql);
        stmt.setInt(1, pcid);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            sale=rs.getInt("sale_id");
        }
        return sale;
    }
    
    private int getorderid(int saleid) throws SQLException{
        int orderid=0;
        String sql="select order_id from food_order where sale_id = ?";
        PreparedStatement stmt=con.prepareStatement(sql);
        stmt.setInt(1, saleid);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            orderid=rs.getInt("order_id");
        }
        return orderid;
    }
    
    public void setOrderDetails(String clientName, ArrayList<foods> cartList) throws SQLException {
    // Set customer name
    this.cartList=cartList;
    this.clientname=clientName;
    this.pcid=pcid = Integer.parseInt(clientName.replaceAll("[^0-9]", ""));
    saleid=getsaleid(pcid);
    lbroomno.setText("Room-"+getroomno(pcid));
    lbname.setText( getusername(pcid));
    
    // Set timestamp
    lbtimestamp.setText(java.time.LocalDateTime.now().format(
    java.time.format.DateTimeFormatter.ofPattern("h:mm a")));
    
    // Calculate total and create item labels
    double total = 0;
    fooditem_container.getChildren().clear();
    
    for (foods item : cartList) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(20); 
        itemBox.setAlignment(Pos.CENTER_LEFT);
        
        // Item name and quantity (left-aligned)
        Label nameLabel = new Label(item.getName() + " x" + item.getQuantity());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        nameLabel.setPrefWidth(210); // Fixed width for alignment
        
        // Price (right-aligned)
        Label priceLabel = new Label(String.format("$%.2f", item.getPrice() * item.getQuantity()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        priceLabel.setPrefWidth(100);
        
        itemBox.getChildren().addAll(nameLabel, priceLabel);
        fooditem_container.getChildren().add(itemBox);
        
        total += item.getPrice() * item.getQuantity();
    }
    // Set total amount
    lbtotalamount.setText(String.format("$%.2f Ks", total));
}
    
    private int getFoodId(String foodName) throws SQLException {
    String sql = "SELECT food_id FROM foods WHERE food_name = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setString(1, foodName);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("food_id") : -1;
    }
    
    
}
    
    private String getusername(int pcid) throws SQLException{
        String user="";
        String getuserquery = "SELECT customer_name from users where customer_id = "
            + "(SELECT customer_id FROM sale_detail WHERE pc_id= ? AND sale_date = CURRENT_DATE order by sale_id desc limit 1);";
        PreparedStatement stmt=con.prepareStatement(getuserquery);
        stmt.setInt(1, pcid);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            user=rs.getString("customer_name");
        }
        return user;
    }
    private int getroomno(int pcid) throws SQLException{
        int roomno=0;
        String roomsql = "Select room_id from sale_detail where pc_id = ? and status_id = 2";
        PreparedStatement stmt=con.prepareStatement(roomsql);
        stmt.setInt(1, pcid);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            roomno=rs.getInt("room_id");
        }
        return roomno;
    }
    
        public String getClientName() {
        return clientname;
    }
    
    public ArrayList<foods> getCartList() {
        return new ArrayList<>(cartList); 
    }
    
}

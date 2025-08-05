/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import model.fooditem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.TableCell;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class InvoiceController implements Initializable {

    @FXML
    private Button btnpay;
    @FXML
    private TableView<fooditem> tbfoodsale;
    @FXML
    private TableColumn<fooditem, String> tbitemname;
    @FXML
    private TableColumn<fooditem, Integer> tbitemqty;
    @FXML
    private TableColumn<fooditem, Double> tbitemprice;
    @FXML
    private TableColumn<fooditem, Double> tbamount;
    @FXML
    private Label lbtotalamount;
    @FXML
    private Label lbpackagename;
    @FXML
    private Label lbperiod;
    @FXML
    private Label lbpackageprice;
    @FXML
    private Label lbgameamount;
    @FXML
    private Label lbusername;
    
    Connection connection;
    private int saleid;
    private AnchorPane parentCard;
    private int totalamount;
    private InvoicepaneController invoicepane=InvoicepaneController.getinstance();
    
    @FXML
    private Button btnprint;
    @FXML
    private AnchorPane invoicecard;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DbConnection db = new DbConnection();
        try {
            connection = db.getConnection();
            setupTableColumns();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(InvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            showError("Database connection failed: " + ex.getMessage());
        }
    }    

    private void setupTableColumns() {
        tbitemname.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        tbitemqty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tbitemprice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tbamount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        // Format price columns
        tbitemprice.setCellFactory(column -> new TableCell<fooditem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? "" : String.format("%.2fKs", price));
            }
        });
        
        tbamount.setCellFactory(column -> new TableCell<fooditem, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty ? "" : String.format("%.2fKs", amount));
            }
        });
    }

    @FXML
    private void btnpayaction(ActionEvent event) throws SQLException {
        String sql="update sale set payment_status = 'paid' where sale_id = ?";
        PreparedStatement stmt=connection.prepareStatement(sql);
        stmt.setInt(1, saleid);
        stmt.executeUpdate();
        
        InvoicepaneController.instance.removeInvoiceBySaleId(saleid);
    }

    public void setdata(int saleid) {
        this.saleid = saleid;
        loadInvoiceData();
        System.out.println("This table setup work too!");
    }

    private void loadInvoiceData() {
        try {
            // Load package details
            String packageQuery = "SELECT p.package_type, sd.period, c.customer_name, " +
                                "p.price AS package_price, s.total_price " +
                                "FROM sale s " +
                                "JOIN sale_detail sd ON s.sale_id = sd.sale_id " +
                                "JOIN users c ON s.customer_id = c.customer_id " +
                                "JOIN package p ON sd.package_id = p.package_id " +
                                "WHERE s.sale_id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(packageQuery)) {
                stmt.setInt(1, saleid);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    lbpackagename.setText(rs.getString("package_type"));
                    lbperiod.setText(rs.getString("period")+" hour");
                    lbusername.setText(rs.getString("customer_name"));
                    lbpackageprice.setText(String.format("%.2fKs", rs.getDouble("package_price")));
                    lbgameamount.setText(String.format("%.2fKs", rs.getDouble("total_price")));
                    lbtotalamount.setText(Integer.toString(gettotalamount(saleid))+"Ks");
                }
            }
            
            // Load food items
            List<fooditem> foodItems = getFoodItemsForOrder(saleid);
            ObservableList<fooditem> observableList = FXCollections.observableArrayList(foodItems);
            tbfoodsale.setItems(observableList);
            
        } catch (SQLException ex) {
            Logger.getLogger(InvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            showError("Failed to load invoice data: " + ex.getMessage());
        }
    }
    
    public List<fooditem> getFoodItemsForOrder(int saleId) throws SQLException {
        List<fooditem> foodItems = new ArrayList<>();
        String query = "SELECT f.food_name, fod.qty, f.price, (fod.qty * f.price) AS amount " +
                       "FROM food_order_detail fod " +
                       "JOIN foods f ON fod.food_id = f.food_id " +
                       "JOIN food_order fo ON fod.order_id = fo.order_id " +
                       "WHERE fo.sale_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fooditem item = new fooditem(
                    rs.getString("food_name"),
                    rs.getInt("qty"),
                    rs.getDouble("price"),
                    rs.getDouble("amount")
                );
                foodItems.add(item);
            }
        }
        return foodItems;
    }
    
    private int gettotalamount(int saleid) throws SQLException{

        String sql = "SELECT SUM(total_price + total_food_price) AS combined_total_amount FROM sale where sale_id = ?;";
        PreparedStatement stmt=connection.prepareStatement(sql);
        stmt.setInt(1, saleid);
        ResultSet rs= stmt.executeQuery();
        if(rs.next()){
            totalamount = rs.getInt("combined_total_amount");
        }
        return totalamount;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setparentcard(AnchorPane card) {
            this.parentCard = card;
            card.getProperties().put("controller", this);
        }

    int getSaleId() {
        return saleid;
    }
    
    public AnchorPane getParentCard() {
        return parentCard;
    }

    @FXML
    private void btnprintaction(ActionEvent event) {
        Printer printer=Printer.getDefaultPrinter();
        if(printer==null){
            System.out.println("No Printers Installed.");
            return;
        }
        PrinterJob job= PrinterJob.createPrinterJob(printer);
        
        if(job!=null && job.showPrintDialog(invoicecard.getScene().getWindow())){
            boolean success =job.printPage(invoicecard);
            if(success){
                job.endJob();
                System.out.println("Voucher printed successfully.");
            }else{
                System.out.println("Printing failed.");
            }
        }

    }
}
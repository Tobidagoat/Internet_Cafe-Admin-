/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import controller.Update_CustomerController;
import javafx.scene.input.KeyEvent;

import model.customer;
/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class CustomerController implements Initializable {


    @FXML
    private TableView<customer> cTable;
    @FXML
    private TableColumn<?, ?> cid;
    @FXML
    private TableColumn<?, ?> cName;
    @FXML
    private TableColumn<?, ?> cPhno;
    @FXML
    private TableColumn<?, ?> cEmail;
    @FXML
    private TableColumn<?, ?> cProfile;
    @FXML
    private TableColumn<customer, String> cDate;
    @FXML
    private MenuButton menuFilter;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
      @FXML
    private Label txtActiveCustomer;

    @FXML
    private Label txtLoyalCustomer1;

    @FXML
    private Label txtLoyalCustomer2;

    @FXML
    private Label txtLoyalCustomer3;
       @FXML
    private Label txtTotalCustomer;

    
    

    /**
     * Initializes the controller class.
     */
    
    ObservableList<customer> customerList;
     Statement st;
    PreparedStatement pst;
    ResultSet rs;
    Connection con = null;
    Parent root;
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        customerList = FXCollections.observableArrayList();
        
        DbConnection db = new DbConnection();
        
        try {
            con = db.getConnection();
            loadTable();
            
            cDate.setCellFactory(col -> new TableCell<customer, String>() {
                private final Label lbDate = new Label();
                private final Button editButton = new Button("⋮");
                private final ContextMenu rightMenu = new ContextMenu();
                MenuItem editMenu = new MenuItem("Edit");
                MenuItem deleteMenu = new MenuItem("Delete");
                
                private final HBox editBtnContainer = new HBox();

        {
            rightMenu.getItems().addAll(editMenu,deleteMenu);
            
            
        editButton.setStyle("-fx-background-color: transparent; -fx-font-size: 30px; -fx-text-fill:white;");
        lbDate.setStyle("-fx-font-size: 14px;");
        editButton.setPadding(new Insets(0));
        HBox.setHgrow(lbDate, Priority.ALWAYS);
         editBtnContainer.setMinHeight(40);
        editBtnContainer.setPrefHeight(40);
        editBtnContainer.setMaxHeight(40);
        
        //Menu style
        editMenu.setStyle("-fx-font-size:14px; -fx-padding:0px;");
        deleteMenu.setStyle("-fx-font-size:14px; -fx-padding:0px;");
  

        editBtnContainer.getChildren().addAll(lbDate, editButton);
        editBtnContainer.setAlignment(Pos.TOP_LEFT);
        editBtnContainer.setSpacing(80);
        
        
        //Menu action
        editButton.setOnAction(e->{
            rightMenu.show(editButton,Side.RIGHT,0,0);
            
        });
        //Edit  action
         editMenu.setOnAction(e -> {
             
              customer c =(customer)cTable.getSelectionModel().getSelectedItem();
              String ccName= c.getName();
              String ccPhone = c.getPhno();
              String ccEmail = c.getEmail();
              int ccId = c.getCid();
             
              
              
             

             FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Update_Customer.fxml"));
             
            try {
                Parent editRoot= loader.load();
                Update_CustomerController controller = loader.getController();
                controller.UpdateData(ccId, ccName, ccPhone, ccEmail);
                
                
                controller.setOnCustomerAdded(()->{
            try {
                
                loadTable();
            } catch (SQLException ex) {
                Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
             
             
             
         });
          
          
                
                    Stage editStage = new Stage();
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.setScene(new Scene(editRoot));
                editStage.setTitle("Edit Customer");
                editStage.setResizable(false);
                editStage.showAndWait();
                
                
            } catch (IOException ex) {
                Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         
         
            
    });
        deleteMenu.setOnAction(e->{
            customer c =(customer)cTable.getSelectionModel().getSelectedItem();
            int ccId = c.getCid();
            
            String sql ="delete from users where customer_id=?";
                try {
                    pst=con.prepareStatement(sql);
                    pst.setInt(1, ccId);
                    
                    pst.executeUpdate();
                    loadTable();
                } catch (SQLException ex) {
                    Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            
            
        });
        
        
        }

    
      @Override
    protected void updateItem(String date, boolean empty) {
        super.updateItem(date, empty);

        if (empty || date == null) {
            setGraphic(null);
        } else {
            lbDate.setText(date);
            setGraphic(editBtnContainer);

            TableRow<?> row = getTableRow();
            boolean showButton = row != null && (row.isSelected() || isFocused());
            editButton.setVisible(showButton);

            // Reactive listeners
            row.selectedProperty().addListener((obs, wasSel, isNowSel) -> {
                editButton.setVisible(isNowSel || isFocused());
            });

            focusedProperty().addListener((obs, wasFocus, isNowFocus) -> {
                editButton.setVisible(isNowFocus || row.isSelected());
            });
        }
    }});

            
                        
            
           
            
        } catch (ClassNotFoundException ex) {
            
            
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
    
   
    
    @FXML
    private void HandleSearchAction(ActionEvent event) throws SQLException {
        
        if(txtSearch.getText().isEmpty()){
            initCustomerList();
            cTable.setItems(customerList);
        }else{
            String sql = "select * from users where customer_id like ? or customer_name like ?";
            
            pst = con.prepareStatement(sql);
            pst.setString(1, txtSearch.getText()+"%");
            pst.setString(2, txtSearch.getText()+"%");
            rs = pst.executeQuery();
            boolean found = false;
            customerList.removeAll(customerList);
            
            while(rs.next()){
                found =true;
                customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date")));
            }
            if(!found){
                 initCustomerList();
                 cTable.setItems(customerList);
                 JOptionPane.showMessageDialog(null, " not found!");
                
            }
            
        }
    }
    
    @FXML
    private void HandleSearchBarAction(ActionEvent event) {
        btnSearch.fire();
    }
        
    
    
    @FXML
    private void HandleAddCustomerAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/addCustomer.fxml"));
         Parent modalRoot = loader.load();
         
         AddCustomerController controller = loader.getController();
         
         controller.setOnCustomerAdded(()->{
            try {
                
                loadTable();
            } catch (SQLException ex) {
                Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
             
             
             
         });
         
         Stage modalStage = new Stage();
         modalStage.initModality(Modality.APPLICATION_MODAL);
         modalStage.setScene(new Scene(modalRoot));
         modalStage.setTitle("Add New Customer");
         modalStage.setResizable(false);
         modalStage.showAndWait();
        
        
        
        
    }
    
    @FXML
    private void HandleFilterAction(ActionEvent event) {
    }
    
     @FXML
    void HandleAutoSearchAction(KeyEvent event) throws SQLException {
        
        if(txtSearch.getText().isEmpty()){
            initCustomerList();
            cTable.setItems(customerList);
        }else{
            String sql = "select * from users where customer_id like ? or customer_name like ?";
            
            pst = con.prepareStatement(sql);
            pst.setString(1, txtSearch.getText()+"%");
            pst.setString(2, txtSearch.getText()+"%");
            rs = pst.executeQuery();
            boolean found = false;
            customerList.removeAll(customerList);
            
            while(rs.next()){
                found =true;
                customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date")));
            }
            
            
        }


    }
    
    
    
    public void initCustomerList() throws SQLException{
        customerList = FXCollections.observableArrayList();
        String sql = "select * from users";
        st= con.createStatement();
        rs =st.executeQuery(sql);
        
        while(rs.next()){
          customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date")));

        }
    }
    public void loadTable() throws SQLException{
         initCustomerList();
            
            cid.setCellValueFactory(new PropertyValueFactory("cid"));
            cName.setCellValueFactory(new PropertyValueFactory("name"));
            cPhno.setCellValueFactory(new PropertyValueFactory("phno"));
            cEmail.setCellValueFactory(new PropertyValueFactory("email"));
            cProfile.setCellValueFactory(new PropertyValueFactory("profile"));
            cDate.setCellValueFactory(new PropertyValueFactory("date"));
            
           
            
            cTable.setItems(customerList);
            
            String sql = "select count(*) from users";
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            if(rs.next()){
                int count  = rs.getInt(1);
                txtTotalCustomer.setText(count+"");
                
                
                
              
            }

    }
}

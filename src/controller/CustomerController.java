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
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

import model.customer;
/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class CustomerController implements Initializable {


    @FXML
    private ToggleButton btnHome;
    @FXML
    private ToggleGroup sideBarToggleGroup;
    @FXML
    private ToggleButton btnData;
    @FXML
    private ToggleButton btnBooking;
    @FXML
    private ToggleButton btnFood;
    @FXML
    private ToggleButton btnCustomer;
    @FXML
    private ToggleButton btnLogout;
    @FXML
    private ToggleButton btnSetting;
    @FXML
    private Button btnAddCustomer;
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
    private TableColumn<?, ?> cDate;
    @FXML
    private MenuButton menuFilter;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
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
            
           
            
        } catch (ClassNotFoundException ex) {
            
            
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
    
    @FXML
    private void HandleHomeAction(ActionEvent event) {
    }

    @FXML
    private void HandleDataAction(ActionEvent event) {
    }

    @FXML
    private void HandleBookingAction(ActionEvent event) {
    }

    @FXML
    private void HandleFoodAction(ActionEvent event) {
    }

    @FXML
    private void HandleCustomerAction(ActionEvent event) {
    }

    @FXML
    private void HandleLogoutAction(ActionEvent event) {
    }

    @FXML
    private void HandleSettingAction(ActionEvent event) {
    }
    
    @FXML
    private void HandleSearchAction(ActionEvent event) throws SQLException {
        
        if(txtSearch.getText().isEmpty()){
            initCustomerList();
            cTable.setItems(customerList);
        }else{
            String sql = "select * from user where customer_id like ? or customer_name like ?";
            
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
    
    
    
    public void initCustomerList() throws SQLException{
        customerList = FXCollections.observableArrayList();
        String sql = "select * from user";
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
    }
}

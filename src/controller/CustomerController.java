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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;

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
    @FXML
    private TableColumn<customer, String> cStatus;
     @FXML
    private ImageView imgFir;

    @FXML
    private ImageView imgSec;

    @FXML
    private ImageView imgThi;

    
    

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
            
             //top 3 customers setup
             
//             SetUpimg(imgFir);
//             SetUpimg(imgSec);
//             SetUpimg(imgThi);
             
        
             
            String sql = "SELECT u.customer_name as c_name,profile_pic as img,SUM(s.total_price + s.total_food_price) AS total_spent FROM sale s JOIN users u ON s.customer_id = u.customer_id GROUP BY s.customer_id, u.customer_name ORDER BY total_spent DESC LIMIT 3;";
            st = con.prepareStatement(sql);
            rs = st.executeQuery(sql);
            int i =1;
            while(rs.next()){
                String name = rs.getString("c_name");
                String img = rs.getString("img");
                
                
                switch (i) {
                    case 1 -> {txtLoyalCustomer1.setText(name);
                               loadimg(img, imgFir);
                    }
                    case 2 ->{ txtLoyalCustomer2.setText(name);
                                loadimg(img, imgSec);
                                
                    }
                    case 3 ->{ txtLoyalCustomer3.setText(name);
                                loadimg(img, imgThi);
                               
                    }
                }
                i++;
            }
            
             } catch (ClassNotFoundException ex) {
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
       
        
        
        //table edit setup
            cStatus.setCellFactory(col -> new TableCell<customer, String>() {

    private final Label lbstatus = new Label();
    private final Button editButton = new Button("⋮");
    private final ContextMenu rightMenu = new ContextMenu();
    private final MenuItem editMenu = new MenuItem("Edit");
    private final MenuItem banMenu = new MenuItem(); // was deleteMenu

    private final HBox editBtnContainer = new HBox();

    {
        rightMenu.getItems().addAll(editMenu, banMenu);

        editButton.setStyle("-fx-background-color: transparent; -fx-font-size: 30px; -fx-text-fill:white;");
        lbstatus.setStyle("-fx-font-size: 14px;");
        editButton.setPadding(new Insets(0));
        HBox.setHgrow(lbstatus, Priority.ALWAYS);
        editBtnContainer.setMinHeight(40);
        editBtnContainer.setPrefHeight(40);
        editBtnContainer.setMaxHeight(40);

        // Menu item styles
        editMenu.setStyle("-fx-font-size:14px; -fx-padding:0px;");
        banMenu.setStyle("-fx-font-size:14px; -fx-padding:0px;");

        editBtnContainer.getChildren().addAll(lbstatus, editButton);
        editBtnContainer.setAlignment(Pos.TOP_LEFT);
        editBtnContainer.setSpacing(80);

        // Context menu trigger
        editButton.setOnAction(e -> {
            rightMenu.show(editButton, Side.RIGHT, 0, 0);
        });

        // Edit action (unchanged)
        editMenu.setOnAction(e -> {
            customer c = getTableView().getItems().get(getIndex());
            String ccName = c.getName();
            String ccPhone = c.getPhno();
            String ccEmail = c.getEmail();
            int ccId = c.getCid();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Update_Customer.fxml"));
            try {
                Parent editRoot = loader.load();
                Update_CustomerController controller = loader.getController();
                controller.UpdateData(ccId, ccName, ccPhone, ccEmail);

                controller.setOnCustomerAdded1(() -> {
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

        // Ban/Unban logic is now handled in updateItem()
    }

    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);

        if (empty || status == null) {
            setGraphic(null);
        } else {
            customer c = getTableView().getItems().get(getIndex());

            lbstatus.setText(status);
            setGraphic(editBtnContainer);

            TableRow<?> row = getTableRow();
            boolean showButton = row != null && (row.isSelected() || isFocused());
            editButton.setVisible(showButton);

            // Show/hide logic
            row.selectedProperty().addListener((obs, wasSel, isNowSel) -> {
                editButton.setVisible(isNowSel || isFocused());
            });

            focusedProperty().addListener((obs, wasFocus, isNowFocus) -> {
                editButton.setVisible(isNowFocus || row.isSelected());
            });

            // Dynamically change ban menu text and logic
            String currentStatus = c.getStatus();
            if ("ban".equalsIgnoreCase(currentStatus)) {
                banMenu.setText("Unban");
            } else {
                banMenu.setText("Ban");
            }

            banMenu.setOnAction(e -> {
                String newStatus = "ban".equalsIgnoreCase(currentStatus) ? "active" : "ban";
                c.setStatus(newStatus);

                // OPTIONAL: Save to DB
                String sql = "UPDATE users SET status = ? WHERE customer_id = ?";
                try {
                    pst = con.prepareStatement(sql);
                    pst.setString(1, newStatus);
                    pst.setInt(2, c.getCid());
                    pst.executeUpdate();
                    loadTable();
                } catch (SQLException ex) {
                    Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
                }

                getTableView().refresh();
            });
        }
    }
});


            
                        
            
           
            
       
        
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
            pst.setString(2,"%"+ txtSearch.getText()+"%");
            rs = pst.executeQuery();
            boolean found = false;
            customerList.removeAll(customerList);
            
            while(rs.next()){
                found =true;
                customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date"),rs.getString("status")));
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
                customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date"),rs.getString("status")));
            }
            
            
        }


    }
    
    
    
    public void initCustomerList() throws SQLException{
        customerList = FXCollections.observableArrayList();
        String sql = "select * from users";
        st= con.createStatement();
        rs =st.executeQuery(sql);
        
        while(rs.next()){
          customerList.add(new customer(rs.getInt("customer_id"),rs.getString("customer_name"),rs.getString("ph_no"),rs.getString("e_mail"),rs.getString("profile_pic"),rs.getString("date"),rs.getString("status")));

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
            cStatus.setCellValueFactory(new PropertyValueFactory("status"));
            
           
            
            cTable.setItems(customerList);
            
            String sql = "select count(*) as total from users";
            st = con.prepareStatement(sql);
             rs= st.executeQuery(sql);
            if(rs.next()){
                
                txtTotalCustomer.setText(Integer.toString(rs.getInt("total")));
                
                
            String sqll = "select count(customer_id) as cust from sale_detail where status_id =2 group by customer_id;";
            st = con.prepareStatement(sqll);
            rs = st.executeQuery(sqll);
            if(rs.next()){
                txtActiveCustomer.setText(Integer.toString(rs.getInt("cust")));
            }
                
            
                
                
                
              
            }

    }
    private void loadimg(String imgName, ImageView imageview) {
        
              // FOR TESTING NEED TO CHANGE AFTER COMPILE
            Path targetDir = Paths.get("D:/Internet_cafe_2.0/Internet_Cafe-Admin-/src/img");
            
            //USE AFTER COMPILE AS JAR FR
//            Path targetDir = Paths.get(System.getProperty("user.dir"), "img");
        
    // Apply circular clip
    
    Circle clip = new Circle(
        imageview.getFitWidth() / 2,
        imageview.getFitHeight() / 2,
        imageview.getFitWidth() / 2
    );
    imageview.setClip(clip);

    // Defensive check for null or empty imgName
    if (imgName == null || imgName.trim().isEmpty()) {
        setDefaultImage(imageview);
        return;
    }

    // File path setup
    File file = new File(targetDir + File.separator + imgName);

    if (file.exists()) {
        Image image = new Image(
            file.toURI().toString(),
            550, 550, true, true, true
        );
        imageview.setImage(image);
    } else {
        setDefaultImage(imageview);
    }

    imageview.setPreserveRatio(true);
    imageview.setSmooth(true);
    imageview.setCache(true);
}

    private void setDefaultImage(ImageView imageview) {
    try {
        // Use resource fallback or a static file path as backup
        Image defaultImg = new Image(getClass().getResourceAsStream("/img/Default_user.png"));
        imageview.setImage(defaultImg);
    } catch (Exception e) {
        System.out.println("Default image not found: " + e.getMessage());
    }
}

    

}

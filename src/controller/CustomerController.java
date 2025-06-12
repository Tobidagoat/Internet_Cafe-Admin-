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
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
    private TableView<?> cTable;
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
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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

}

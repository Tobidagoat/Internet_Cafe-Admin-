/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import jdk.jshell.execution.LoaderDelegate;

/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class DefaultController implements Initializable {

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
    private AnchorPane mainContentAnchorPane;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
         sideBarToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
        if (newToggle == null) {
            // Re-select the previous toggle
            sideBarToggleGroup.selectToggle(oldToggle);
        }
    });
        try {
            loadUI("/view/Customer.fxml");
            btnHome.setSelected(true);
            // TODO
        } catch (IOException ex) {
            Logger.getLogger(DefaultController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void HandleSwitchHomeAction(ActionEvent event) {
    }

    @FXML
    private void HandleSwitchDataAction(ActionEvent event) throws IOException {
       loadUI("/view/data.fxml");
       
   
        

        
    }

    @FXML
    private void HandleSwitchBookingAction(ActionEvent event) throws IOException {
        loadUI("/view/room.fxml");
    }

    @FXML
    private void HandleSwitchFoodAction(ActionEvent event) throws IOException {
        loadUI("/view/login.fxml");
    }

    @FXML
    private void HandleSwitchCustomerAction(ActionEvent event) throws IOException {
         loadUI("/view/Customer.fxml");
    }

    @FXML
    private void HandleSwitchLogoutAction(ActionEvent event) {
    }

    @FXML
    private void HandleSwitchSettingAction(ActionEvent event) {
    }
    
    public void loadUI(String fxmlPath) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        AnchorPane newLoadedPane = loader.load();
        mainContentAnchorPane.getChildren().clear();
        mainContentAnchorPane.getChildren().add(newLoadedPane);
        
        AnchorPane.setTopAnchor(newLoadedPane, 0.0);
        AnchorPane.setLeftAnchor(newLoadedPane, 0.0);
         AnchorPane.setBottomAnchor(newLoadedPane, 0.0);
          AnchorPane.setRightAnchor(newLoadedPane, 0.0);
        
        
    }
   
}

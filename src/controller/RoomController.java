/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import model.room;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class RoomController implements Initializable {

    @FXML
    private Button btngeneral;
    @FXML
    private Button btnprivate;
    
    List<room> roomlist;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        for(room room:roomlist){
            //AnchorPane roomcard=createroomcard(room);
        }
    }    

    @FXML
    private void btngeneralaction(ActionEvent event) {
    }

    @FXML
    private void btnprivateaction(ActionEvent event) {
    }

//    private AnchorPane createroomcard(room room) {
//        AnchorPane card=new AnchorPane();
//        card.setPrefSize(274, 274);
//        
//        
//    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class CardController implements Initializable {

    @FXML
    private Label lbroomno;
    @FXML
    private Circle activeball;
    @FXML
    private Label lbactiveno;
    @FXML
    private Label lbroomcategory;
    
    private RoomController controller;
    private String roomid;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    public void setdata(String roomid, String roomtype,RoomController controller){
        this.roomid=roomid;
        this.controller=controller;
        lbroomno.setText(roomid);
        lbroomcategory.setText(roomtype);
        
    }
    @FXML
    public void loadpc(MouseEvent event) throws SQLException, IOException{
        System.out.println("hehehaha");
        System.out.println(roomid);
        String numberstr=roomid.replaceAll("[^0-9]", "");
        int number=Integer.parseInt(numberstr);
        System.out.println(number);
        controller.loadpcforroom(number);
        
//        if(controller != null){
//            controller.loadpcforroom(roomid);
//        }
    }
}

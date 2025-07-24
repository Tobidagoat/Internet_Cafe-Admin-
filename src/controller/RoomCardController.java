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
public class RoomCardController implements Initializable {

    @FXML
    private Label lbroomno;
    @FXML
    private Label lbroomcategory;
    
    private RoomController controller;
    private int roomid;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    public void setdata(int roomid, String roomtype,RoomController controller){
        this.roomid=roomid;
        this.controller=controller;
        lbroomno.setText("ROOM - "+roomid);
        lbroomcategory.setText(roomtype);
        
    }
    @FXML
    public void loadpc(MouseEvent event) throws SQLException, IOException, ClassNotFoundException{
        controller.loadpcforroom(roomid);
    }
}

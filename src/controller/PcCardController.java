/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class PcCardController implements Initializable {

    @FXML
    private Label lbpcno;
    @FXML
    private Label lbstatus;
    
    private RoomController controller;
    private String pcno;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setpcinfo(String pcno, RoomController controller){
        this.pcno=pcno;
        this.controller=controller;
        lbpcno.setText(pcno);
        
    }
    
    @FXML
    private void loadpackage(MouseEvent event) throws IOException {
        controller.showpackages(pcno);
    }
    
}

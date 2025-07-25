/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class InvoicepaneController implements Initializable {

    @FXML
    private HBox invoicecontainer;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
    
    public void addinvoice(int saleid) throws IOException{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/invoice.fxml"));
        AnchorPane card=loader.load();
        InvoiceController controller=loader.getController();
        controller.setdata(saleid);
        controller.setparentcard(card);
        triggernotificationevent();
    }

    private void triggernotificationevent() {
        
    }
    
}

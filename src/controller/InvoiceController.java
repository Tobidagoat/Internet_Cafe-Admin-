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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class InvoiceController implements Initializable {

    @FXML
    private Button btnpay;
    @FXML
    private TableView<?> tbfoodsale;
    @FXML
    private TableColumn<?, ?> tbitemname;
    @FXML
    private TableColumn<?, ?> tbitemqty;
    @FXML
    private TableColumn<?, ?> tbitemprice;
    @FXML
    private TableColumn<?, ?> tbamount;
    @FXML
    private Label lbtotalamount;
    @FXML
    private Label lbpackagename;
    @FXML
    private Label lbperiod;
    @FXML
    private Label lbpackageprice;
    @FXML
    private Label lbgameamount;
    @FXML
    private Label lbusername;
    
    private int saleid;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void btnpayaction(ActionEvent event) {
    }

    void setdata(int saleid) {
        this.saleid=saleid;
        
    }

    void setparentcard(AnchorPane card) {
    }
    
    private void getdata(int saleid){
        String sql="Select * from ";
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import database.DbConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
//import model.room;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    @FXML
    private FlowPane cardcontainer;
    @FXML
    private AnchorPane roompane;
    @FXML
    private FlowPane pccontainer;
    @FXML
    private AnchorPane pcpane;
    
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
//    List<room> roomlist;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DbConnection db=new DbConnection();
        try {
            con=db.getConnection();
            loadrooms("general");
        } catch (ClassNotFoundException ex) {
            
        } catch (SQLException ex) {
            Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }    
    
public void loadrooms(String roomcategory) throws SQLException, IOException{
       
        roompane.setVisible(true);
        pcpane.setVisible(false);
    cardcontainer.getChildren().clear();
    String sql="Select * from rooms where room_category Like ?";
    pst=con.prepareStatement(sql);
    pst.setString(1, roomcategory);
    rs=pst.executeQuery();
    
    while(rs.next()){
        String id=rs.getString("room_id");
        String type=rs.getString("room_type");
        
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/roomcard.fxml"));
        AnchorPane card=loader.load();
        
        RoomCardController cardcontrol=loader.getController();
        
        cardcontrol.setdata("Room - "+id, type,this);
        
        //animation!!
        FadeTransition ft=new FadeTransition(Duration.millis(500),card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        cardcontainer.getChildren().add(card);
    }
}
    public void loadpcforroom(int roomid) throws SQLException, IOException{
        pcpane.setVisible(true);
        roompane.setVisible(false);
        
        
        pccontainer.getChildren().clear();
        pst=con.prepareStatement("Select * from pcs where room_id = ?");
        pst.setInt(1, roomid);
        rs=pst.executeQuery();
        
        while(rs.next()){
            String no=rs.getString("pc_no");            
            String pcname="PC - "+no;
           
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/pccard.fxml"));
            AnchorPane card=loader.load();
            
            PcCardController cardcontrol=loader.getController();
        
            cardcontrol.setpcinfo(pcname,this);
            
            //animation!!
            FadeTransition ft=new FadeTransition(Duration.millis(500),card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            
            pccontainer.getChildren().add(card);
            
        }        
    }
    
    public void showpackages(String pcno) throws IOException{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/package.fxml"));
        AnchorPane popup=loader.load();
        
        Stage popupstage=new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Select Package");
        popupstage.setScene(new Scene(popup));
        popupstage.show();
        
//        PackageController popupcontroller=loader.getController();
//        popupcontroller.setPCID(pcid);
//        
    }
    
    @FXML
    private void btngeneralaction(ActionEvent event) throws SQLException, IOException {
        loadrooms("general");
        btngeneral.setStyle("-fx-background-color: #ffffff;"
                + "-fx-border-color:  #494949;"
                + "-fx-text-fill:  #141619;"
                + "-fx-background-radius:  10px 0 0 0;"
                + "-fx-border-radius:  10px 0 0 0;");
        btnprivate.setStyle("-fx-background-color:  #141619;"
                + "-fx-border-color:  #494949;"
                + "-fx-text-fill: #ffffff;"
                + "-fx-background-radius:  0 10px 0 0;"
                + "-fx-border-radius:  0 10px 0 0;");
    }

    @FXML
    private void btnprivateaction(ActionEvent event) throws SQLException, IOException {
        loadrooms("private");
       btnprivate.setStyle("-fx-background-color: #ffffff;"
                + "-fx-border-color:  #494949;"
                + "-fx-text-fill:  #141619;"
                + "-fx-background-radius:  0 10px 0 0;"
                + "-fx-border-radius:  0 10px 0 0;");
        btngeneral.setStyle("-fx-background-color:  #141619;"
                + "-fx-border-color:  #494949;"
                + "-fx-text-fill: #ffffff;"
                + "-fx-background-radius:  10px 0 0 0;"
                + "-fx-border-radius:  10px 0 0 0;");
    }
   
    
    
}

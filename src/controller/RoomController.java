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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
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
    private ScrollPane pcpane;
    
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    private String roomtype;
    private int pcid;
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
    //method for separating rooms with general and private
    public void loadrooms(String roomcategory) throws SQLException, IOException{

            roompane.setVisible(true);
            pcpane.setVisible(false);
        cardcontainer.getChildren().clear();
        String sql="Select * from rooms where room_category Like ?";
        pst=con.prepareStatement(sql);
        pst.setString(1, roomcategory);
        rs=pst.executeQuery();

        while(rs.next()){
            int id=rs.getInt("room_id");
            roomtype=rs.getString("room_type");

            FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/roomcard.fxml"));
            AnchorPane card=loader.load();

            RoomCardController cardcontrol=loader.getController();

            cardcontrol.setdata(id, roomtype,this);

            //animation!!
            FadeTransition ft=new FadeTransition(Duration.millis(500),card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            cardcontainer.getChildren().add(card);
        }
    }
    //method for loading pcs when u tap a roomcard
    public void loadpcforroom(int roomid) throws SQLException, IOException, ClassNotFoundException{
        pcpane.setVisible(true);
        roompane.setVisible(false);
        
        pccontainer.getChildren().clear();
        pst=con.prepareStatement("Select * from pcs where room_id = ?");
        pst.setInt(1, roomid);
        rs=pst.executeQuery();
        
        while(rs.next()){
            int no=rs.getInt("pc_no");      
            String pcname="PC - "+no;
            int pcid=rs.getInt("pc_id");
           
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/pccard.fxml"));
            AnchorPane card=loader.load();
            
            PcCardController cardcontrol=loader.getController();
            cardcontrol.setRoomType(roomtype);
            cardcontrol.setpcinfo(pcname,this,roomid,pcid);
            
            //animation!!
            FadeTransition ft=new FadeTransition(Duration.millis(500),card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            
            pccontainer.getChildren().add(card);
            
        }        
    }
    
    public void showuserlist(int pcid,int roomid) throws IOException{
        
        FXMLLoader userloader=new FXMLLoader(getClass().getResource("/view/userlist.fxml"));
        AnchorPane userpopup=userloader.load();
        UserlistController usercontroller=userloader.getController();
        usercontroller.setroomtype(roomtype);
        
        Stage popupstage=new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("");
        popupstage.setScene(new Scene(userpopup));
        popupstage.showAndWait();
    }
    //load the modal when u tap a pc
    public void showpackages(int pcid,int roomid) throws IOException, ClassNotFoundException, SQLException{
        
        
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/view/package.fxml"));
        AnchorPane popup=loader.load();
        
        PackageController controller=loader.getController();
        controller.setpcandroom(pcid,roomid);
        String roomtype=controller.getroomtype(roomid);
        controller.setroominfo(roomtype, pcid, roomid);
                
        Stage popupstage=new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Select Package");
        popupstage.setScene(new Scene(popup));
        popupstage.showAndWait();
        
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

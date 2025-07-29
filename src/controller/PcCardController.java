/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.swing.JOptionPane;
import org.kordamp.ikonli.javafx.FontIcon;



   
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
    @FXML
    private Button btnterminate;
    @FXML
    private FontIcon isconnected;
    
    private RoomController controller;
    private String pcno;
    public int pcid;
    private int roomid;
    private String roomtype;
    public AnchorPane card;
    private String str;
    private int statusid;
    private boolean checkcon;
    server s =server.getInstance();
    ArrayList<String> userlist;
    
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    private boolean isUnlocked = false;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isconnected.setVisible(false);
        DbConnection db=new DbConnection();
        
        try {
            con=db.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PcCardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }    
    public void setRoomType(String roomtype) {
    this.roomtype = roomtype;
    }
    
    public void setpcinfo(String pcno, RoomController controller, int roomid, int pcid, AnchorPane card, int statusid) throws ClassNotFoundException, SQLException{
        this.pcno=pcno;
        this.controller=controller;
        this.roomid=roomid;
        this.pcid=pcid;
        this.roomtype=roomtype;
        this.card=card;
        this.statusid=statusid;
        isconnectedcheck(pcid);
        setStatus(statusid);
        String status=getstatus(statusid);
        lbstatus.setText(status);
        lbpcno.setText(pcno);
        if(checkcon) isconnected.setVisible(true);
        if ("Playing".equalsIgnoreCase(status)) {
            lbstatus.setStyle("-fx-text-fill: red;");
        } else {
            lbstatus.setStyle("-fx-text-fill: green;");
        }
       }
    
   
    
   private boolean isconnectedcheck(int pcid) throws SQLException{
       String sql="select isconnected from pcs where pc_id = ?";
       pst=con.prepareStatement(sql);
       pst.setInt(1, pcid);
       rs=pst.executeQuery();
       if(rs.next()){
           checkcon=rs.getBoolean("isconnected");
       }
       return checkcon;
   }

    private String getstatus(int statusid) throws SQLException {
        String statusname = "";
        String sql="select status_name from status where status_id= ?";
        pst=con.prepareStatement(sql);
        pst.setInt(1, statusid);
        rs=pst.executeQuery();
        while(rs.next()){
            statusname=rs.getString("status_name");
            System.out.println("status is"+statusname);
        }
        return statusname;
        
    }
    
    public void setStatus(int statusid) throws SQLException{
        String status=getstatus(statusid);
        lbstatus.setText(status);
        if(statusid==1){
            isUnlocked=false;
            lbstatus.setStyle("-fx-text-fill: green;");
            btnterminate.setDisable(true);
            btnterminate.setVisible(false);}
        else{
            isUnlocked=true;
            lbstatus.setStyle("-fx-text-fill: red;");
            btnterminate.setDisable(false);
            btnterminate.setVisible(true);
        }
    }
    
    @FXML
    void btnterminateaction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Warning");
        alert.setContentText("Do you want to terminate this session?");
        
        Optional<ButtonType> result = alert.showAndWait();
        boolean accepted= result.isPresent() && result.get() == ButtonType.OK;
            if (accepted) {
                server.sendToClient("TO|pc"+pcid+"|TERMINATE");
                    }
    }
     @FXML
    private void loadpackage(MouseEvent event) throws IOException, ClassNotFoundException, SQLException {
        
        userlist =s.getConnectedClients();
        str="pc"+pcid;
        if(userlist.contains(str)){
            if(!isUnlocked){
            
            if(roomtype.equalsIgnoreCase("general")){
                controller.showuserlist(pcid, roomid);
            }else{
                controller.showpackages(pcid,roomid,card);
            }}
        }
        else{
            JOptionPane.showMessageDialog(null, "This pc is not connected yet.");
        }
       
    }
}

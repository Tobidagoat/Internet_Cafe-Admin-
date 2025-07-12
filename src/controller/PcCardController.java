/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import internet_cafe_admin.server;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javax.swing.JOptionPane;

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
    public int pcid;
    private int roomid;
    private String roomtype;
    server s = new server();
    ArrayList<String> userlist;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        
    }    
    public void setRoomType(String roomtype) {
    this.roomtype = roomtype;
    }
    
    public void setpcinfo(String pcno, RoomController controller,int roomid,int pcid) throws ClassNotFoundException, SQLException{
        this.pcno=pcno;
        this.controller=controller;
        this.roomid=roomid;
        this.pcid=pcid;
        this.roomtype=roomtype;
            
        lbpcno.setText(pcno);
        
    }
    
   
    
    @FXML
    private void loadpackage(MouseEvent event) throws IOException, ClassNotFoundException, SQLException {
        userlist =s.getConnectedClients();
        String s="pc"+pcid;
        if(userlist.contains(s)){        
            if(roomtype.equalsIgnoreCase("general")){
                controller.showuserlist(pcid, roomid);
            }else{
                controller.showpackages(pcid,roomid);
            }
        }
        else{
            JOptionPane.showMessageDialog(null, "This pc is not connected yet.");
        }
       
    }
    
}

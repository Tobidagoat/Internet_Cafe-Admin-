/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javax.swing.JOptionPane;


/**
 * FXML Controller class
 *
 * @author USER
 */
public class UserlistController implements Initializable {

    @FXML
    private VBox listvbox;
    @FXML
    private Button listconfirmbtn;    
    @FXML
    private TextField txtsearch;
    
    private String roomtype;
    private int pcid;
    private String selectedpackage;
    private int roomid;
    private Stage packagestage;
    private Label selectedlabel;
    private int userlimit=1;
    private List<CheckBox>allcheckboxes=new ArrayList<>();
    Connection con;
    ResultSet rs;
    Statement stmt;
    PreparedStatement pst;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       DbConnection db=new DbConnection();
        try {
            con=db.getConnection();
            loaduserlist();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserlistController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UserlistController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    public void setuserinfo(int pcid,String roomtype,int roomid,String package1,Stage packagestage){
        this.pcid=pcid;
        this.roomtype=roomtype;
        this.roomid=roomid;
        this.selectedpackage=package1;
        this.packagestage=packagestage;
    }

    public void setroomtype(String roomtype){
        this.roomtype=roomtype;
        System.out.println(roomtype);
        if(roomtype.equalsIgnoreCase("couple")){
            userlimit=2;
        }else{
            userlimit=1;
        }
    }

    private void loaduserlist() throws SQLException {
        String sql="Select * from users";
        pst=con.prepareStatement(sql);
        rs=pst.executeQuery(sql);
        while(rs.next()){
            String name=rs.getString("customer_name");
            CheckBox check=new CheckBox(name);
            allcheckboxes.add(check);
            check.setOnAction(event->{handleselectlimit();});
            HBox userbox=new HBox(check);
            userbox.setPadding(new Insets(10));
            userbox.setSpacing(10);
            userbox.setStyle("-fx-background-color: #f0f0f0;"
                    + "-fx-border-color: #ccc;"
                    + "-fx-bordr-radius: 8;"
                    + "-fx-background-radius: 8;");
            listvbox.getChildren().add(userbox);
        }
    }
    
    private void handleselectlimit(){
        int selectcount=0;
        for (CheckBox cb: allcheckboxes){
            if(cb.isSelected()){
                selectcount++;
            }
        }
        if (selectcount>=userlimit){
            for(CheckBox cb: allcheckboxes){
                if(!cb.isSelected()){
                    cb.setDisable(true);
                }
            }
        }else{
            for(CheckBox cb: allcheckboxes){
                cb.setDisable(false);
            }
        }
    }
    @FXML
    private void listconfirmaction(ActionEvent event) throws ClassNotFoundException, SQLException {
        List<String> selectuserlist= new ArrayList<>();
        
        
        for(Node box:listvbox.getChildren()){
            if(box instanceof HBox hb){
                for(Node child : hb.getChildren()){
                    if(child instanceof CheckBox cb && cb.isSelected()){
                        selectuserlist.add(cb.getText());
                    }
                }
            }
        }
        if(selectuserlist.isEmpty()){
            JOptionPane.showMessageDialog(null,"Please select a user");
            return;
        }
        
        if(selectedlabel !=null){
            selectedlabel.setText(""+selectuserlist);
        }
        
        if(roomtype.equalsIgnoreCase("general")){
            selectedpackage="Default One";
             String message="User : "+String.join(",", selectuserlist)+"|Package : "+selectedpackage;
             
              
              //sendtoclient(pcid,message);
            System.out.println("Sent to pc: "+pcid);
              System.out.println("Selected nigger: "+message);
            if(packagestage!=null){
                packagestage.close();
            }
        }
        
        System.out.println("Selected users: "+selectuserlist);
        Stage stage=(Stage)listconfirmbtn.getScene().getWindow();
        stage.close();
    }
    public void setlabel(Label label){
        this.selectedlabel=label;
    }
     @FXML
    void txtsearchaction(ActionEvent event) {
        
    }
    
}

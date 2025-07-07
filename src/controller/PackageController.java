/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.scene.control.ComboBox;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author USER
 */
public class PackageController implements Initializable {

    @FXML
    private Button btnsubmit;
    @FXML
    private Button btnback;
    @FXML
    private Button btnuseradd;
    @FXML
    private Label lbusers;    
    @FXML
    private AnchorPane diamondpackage;
    @FXML
    private AnchorPane goldpackage;
    @FXML
    private AnchorPane silverpackage;
    @FXML
    private ComboBox<String> timecombobox;
    
    DbConnection db=new DbConnection();
    Connection con;
    PreparedStatement stmt;
    private String selectedpackage;
    private String pcno;
    private int pcid;
    private int roomid;
    private String roomtype;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timecombobox.getItems().addAll(
        "1 minute", "1 hour", "2 hours", "3 hours"
    );
        timecombobox.setValue("1 hour");
    } 

   

    @FXML
    private void btnbackaction(ActionEvent event) {
        Stage stage=(Stage) btnsubmit.getScene().getWindow();
        stage.close();
        
    }
    
    public void setroominfo(String roomtype,int pcid,int roomid) throws IOException, ClassNotFoundException, SQLException{
        this.roomtype=roomtype;
        this.pcid=pcid;
        this.roomid=roomid;
    }

    @FXML
    private void btnuseraddaction(ActionEvent event) throws IOException, ClassNotFoundException, SQLException {
        FXMLLoader userloader=new FXMLLoader(getClass().getResource("/view/userlist.fxml"));
        AnchorPane popup=userloader.load();
        UserlistController controller=userloader.getController();
        controller.setuserinfo(pcid, roomtype, roomid, "basic_pack",(Stage) btnsubmit.getScene().getWindow());
        controller.setlabel(lbusers);
        controller.setroomtype(getroomtype(roomid));
        Stage popupstage=new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setScene(new Scene(popup));
        popupstage.showAndWait();
    }
    
    @FXML
    void packagecardaction(MouseEvent event) {
        Node node=(Node) event.getSource();
        String packagename = node.getId();
        selectedpackage=packagename;
        System.out.println(packagename);
    }
    
    public void setpcandroom(int pcid,int roomid){
        this.pcid=pcid;
        this.roomid=roomid;
    }
    
    public String getroomtype(int roomid) throws ClassNotFoundException, SQLException{
        String type="";
        con=db.getConnection();
        stmt=con.prepareStatement("Select room_type from rooms where room_id= ?");
        stmt.setInt(1, roomid);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            type=rs.getString("room_type");
        }
        return type;
    }
    
    public List<String> getpcfromroomid(int roomid) throws ClassNotFoundException, SQLException{
        List<String> pclist=new ArrayList<>();
        con=db.getConnection();
        stmt=con.prepareStatement("Select pc_id from pcs where room_id=?");
        stmt.setInt(1, roomid);
        ResultSet rs=stmt.executeQuery();
        while(rs.next()){
            pclist.add(rs.getString("pc_id"));
        }
        return pclist;
    }
    
     @FXML
    private void btnsubmitaction(ActionEvent event) throws ClassNotFoundException, SQLException {
        int selectedroomid=this.roomid;
        int selectedpcid=this.pcid;
        String selectedpackage=this.selectedpackage;
        String selectedusers=this.lbusers.getText();
        String roomtype=getroomtype(selectedroomid);
        String selectedtime = timecombobox.getValue();
        int duration= converttime(selectedtime);
        
        if(selectedusers.isEmpty()||selectedpackage==null){
            JOptionPane.showMessageDialog(null, "Please select both user and a package.");
            return;
        }
        String message="User : "+String.join(",", selectedusers)+"|Package : "+selectedpackage;
        
        
        if(roomtype.equalsIgnoreCase("couple")){
            List<String> pclist=getpcfromroomid(selectedroomid);
            
            for(String pc: pclist){
                //sendtoclient(pc,selectedroomid,selectedpackage,duration);
                System.out.println("Sent to Couple pc: "+pc);
            }
        }else{
            //sendtoclient(selectedpcid,selectedroomid,selectedpackage,duration);
            System.out.println("Sent to pc: "+selectedpcid);
        }
        
        Stage stage=(Stage) btnsubmit.getScene().getWindow();
        stage.close();
        
    }
    
    private int converttime(String time){
        switch(time){
            case "1 minute": return 15 * 60;
            case "1 hour": return 60 * 60;
            case "2 hours": return 2 * 60 * 60;
            case "3 hours": return 3 * 60 * 60;
            default: return 60 * 60;
        }
    }
    
    

    
}

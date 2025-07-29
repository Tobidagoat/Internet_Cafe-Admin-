package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import static internet_cafe_admin.server.sendToClient;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.scene.control.ComboBox;
import javax.swing.JOptionPane;

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

    DbConnection db = new DbConnection();
    Connection con;
    PreparedStatement stmt;
    ResultSet rs;
    private AnchorPane selectedPane = null;
    private RoomController roomController;
    private DashboardController dashboard=DashboardController.getInstance();
    private boolean result=false;
    private String selectedpackage;
    int packageid;
    private String pcno;
    private int pcid;
    private int roomid;
    private String roomtype;
    private String roomcategory;
    private List<Integer> selectedUserIds = new ArrayList<>();
    private List<String> selectedUsernames = new ArrayList<>();

    server s = server.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DbConnection db=new DbConnection();
        try {
            con=db.getConnection();
            
        } catch (ClassNotFoundException ex) {
            
        }
        timecombobox.getItems().addAll("1 minute",
                "6 minutes", "1 hour", "2 hours", "3 hours"
        );
        timecombobox.setValue("1 hour");
    }

    @FXML
    private void btnbackaction(ActionEvent event) {
        Stage stage = (Stage) btnsubmit.getScene().getWindow();
        stage.close();
    }

    public void setroominfo(String roomtype, int pcid, int roomid, String roomcategory) throws IOException, ClassNotFoundException, SQLException {
        this.roomtype = roomtype;
        this.pcid = pcid;
        this.roomid = roomid;
        this.roomcategory=roomcategory;
    }

    @FXML
    private void btnuseraddaction(ActionEvent event) throws IOException, ClassNotFoundException, SQLException {
        FXMLLoader userloader = new FXMLLoader(getClass().getResource("/view/userlist.fxml"));
        AnchorPane popup = userloader.load();
        UserlistController controller = userloader.getController();
        controller.setuserinfo(pcid, roomtype, roomid, "Normal", (Stage) btnsubmit.getScene().getWindow());
        controller.setlabel(lbusers);
        controller.setroomtype(getroomtype(roomid),roomcategory);
        Stage popupstage = new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setScene(new Scene(popup));
        popupstage.showAndWait();
        selectedUserIds=controller.getSelectedUserIds();
        selectedUsernames = controller.getSelectedUserNames();
        lbusers.setText(String.join(", ", selectedUsernames));
    }

    @FXML
    void packagecardaction(MouseEvent event) {
        AnchorPane clickedpane = (AnchorPane) event.getSource();
        if (selectedPane != null) {
        selectedPane.getStyleClass().remove("pricing-card-selected");
    }
        clickedpane.getStyleClass().add("pricing-card-selected");
        selectedPane=clickedpane;
        
        String packagename = clickedpane.getId();
        selectedpackage = packagename;
        System.out.println(packagename);
    }

    public void setpcandroom(int pcid, int roomid) {
        this.pcid = pcid;
        this.roomid = roomid;
    }
    public void setRoomController(RoomController roomController) {
    this.roomController = roomController;
}

    public String getroomtype(int roomid) throws ClassNotFoundException, SQLException {
        String type = "";
        stmt = con.prepareStatement("Select room_type from rooms where room_id= ?");
        stmt.setInt(1, roomid);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            type = rs.getString("room_type");
        }
        return type;
    }

    public List<String> getpcfromroomid(int roomid) throws ClassNotFoundException, SQLException {
        List<String> pclist = new ArrayList<>();
        con = db.getConnection();
        stmt = con.prepareStatement("Select pc_id from pcs where room_id=?");
        stmt.setInt(1, roomid);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            pclist.add(rs.getString("pc_id"));
        }
        return pclist;
    }

    @FXML
    private void btnsubmitaction(ActionEvent event) throws ClassNotFoundException, SQLException {
        int selectedroomid = this.roomid;
        int selectedpcid = this.pcid;
        String selectedpackage = this.selectedpackage;
        String roomtype = getroomtype(selectedroomid);
        String selectedtime = timecombobox.getValue();
        int period=Integer.parseInt(selectedtime.replaceAll("[^0-9]", ""));
        int duration = converttime(selectedtime);
        int packageid=getpackageid(selectedpackage);
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String starttime = now.format(formatter);
        
        if (selectedUserIds.isEmpty() || selectedpackage == null) {
            JOptionPane.showMessageDialog(null, "Please select both user and a package.");
            return;
        }
         
            
        if (roomcategory.equalsIgnoreCase("private")) {
            List<String> pclist = getpcfromroomid(selectedroomid); 
            
             for (int i = 0; i < pclist.size(); i++) {
                String pcName ="pc"+ pclist.get(i);
                int pcID = Integer.parseInt(pcName.replaceAll("[^0-9]", ""));
                System.out.println(pcID);
                int userId = (int) selectedUserIds.get(i);
                String unlockMsg = "UNLOCK|" + pcName + "|" + userId + "|" + selectedroomid + "|" + selectedpackage + "|" + duration;
                sendToClient("TO|" + pcName + "|" + unlockMsg);
                updatestatus(pcID,userId);
                insertdata(userId, pcID, selectedroomid,packageid,starttime,period);
                dashboard.logActivity(pcName+" started session");
                System.out.println(unlockMsg);
            }
        } else {
            int userid=selectedUserIds.get(0);
            
            String pcName = "pc" + selectedpcid;
            sendToClient("TO|" + pcName + "|UNLOCK|" + pcName + "|" + selectedUserIds + "|" + selectedroomid + "|" + selectedpackage + "|" + duration);
//            System.out.println("userid: "+userid+"pcid: "+selectedpcid+"roomid: "+selectedroomid+"packageid: "+packageid+"time: "+starttime+"period: "+period);
            updatestatus(selectedpcid,userid);
            insertdata(userid, selectedpcid, selectedroomid,packageid,starttime,period);
            dashboard.logActivity(pcName+" started session");
            System.out.println("Sent to pc: " + selectedpcid);
        }
        
        result=true;
        Stage stage = (Stage) btnsubmit.getScene().getWindow();
        stage.close();
    }
    
    public boolean getresult(){
        return result;
    }
    
    private int getpackageid(String packagename) throws SQLException{
        
        String sql="select package_id from package where package_type = ? ";
        stmt=con.prepareStatement(sql);
        stmt.setString(1, packagename);
        rs=stmt.executeQuery();
        
        if (rs.next()) {
            packageid = rs.getInt("package_id");
            System.out.println(packageid);
        } else {
            System.out.println("No matching package found.");
        }
        
        return packageid;
    }

    private int converttime(String time) {
        return switch (time) {
            case "1 minute" -> 1 * 60;
            case "6 minutes" -> 6 * 60;
            case "1 hour" -> 60 * 60;
            case "2 hours" -> 2 * 60 * 60;
            case "3 hours" -> 3 * 60 * 60;
            default -> 60 * 60;
        };
    }
    
    private void insertdata(int userid, int pcid, int selectedroomid, int packageid1, String starttime, int period) throws SQLException{
        String sqlinsert="insert into sale_detail(customer_id,pc_id,room_id,package_id,status_id,start_time,period,sale_date) values(?, ?, ?, ?, ?, ?, ?, ?)";
        stmt = con.prepareStatement(sqlinsert);  
        stmt.setInt(1, userid);
            stmt.setInt(2, pcid);
            stmt.setInt(3, selectedroomid);
            stmt.setInt(4, packageid1);
            stmt.setInt(5, 2);
            stmt.setString(6, starttime);
            stmt.setInt(7, period);
            stmt.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
    }

    private void updatestatus(int pcid, int userid) throws SQLException {
        roomController.updateCardStatus(pcid, 2);
        String sql="update pcs set status_id=2 where pc_id= ?";
        stmt=con.prepareStatement(sql);
        stmt.setInt(1, pcid);
        int row=stmt.executeUpdate();
        
        if(row>0)
            System.out.println("status updated successfully");
        else
            System.out.println("No Pc Found to update status");
    }
}

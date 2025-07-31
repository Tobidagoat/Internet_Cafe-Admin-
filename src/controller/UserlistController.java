package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class UserlistController implements Initializable {

    @FXML private VBox listvbox;
    @FXML private Button listconfirmbtn;
    @FXML private TextField txtsearch;

    private String roomtype;
    private String roomcategory;
    private int pcid;
    private int roomid;
    private int userlimit = 1;
    private Label selectedlabel;
    private String selectedpackage;
    private Stage packagestage;
    private RoomController roomController;
    private DashboardController dashboard=DashboardController.getInstance();

    private final List<User> allusers = new ArrayList<>();
    private final List<User> selectedusers = new ArrayList<>();

    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    server s = server.getInstance();

    private static class User {
        int id;
        String name;
        User(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            con = new DbConnection().getConnection();
            loaduserlist();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(UserlistController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setuserinfo(int pcid, String roomtype, int roomid, String package1, Stage packagestage) {
        this.pcid = pcid;
        this.roomtype = roomtype;
        this.roomid = roomid;
        this.selectedpackage = package1;
        this.packagestage = packagestage;
    }

    public void setuserinfo2(int pcid, int roomid) {
        this.pcid = pcid;
        this.roomid = roomid;
    }

    public void setroomtype(String roomtype, String roomcategory) {
        this.roomtype = roomtype;
        this.roomcategory = roomcategory;
        if (roomtype.equalsIgnoreCase("couple")) userlimit = 2;
        else if (roomtype.equalsIgnoreCase("team")) userlimit = 4;
        else if (roomtype.equalsIgnoreCase("squad")) userlimit = 8;
        else userlimit = 1;
    }

    private void loaduserlist() throws SQLException {
        pst = con.prepareStatement("SELECT customer_id, customer_name FROM users where status = 'active'");
        rs = pst.executeQuery();
        allusers.clear();
        while (rs.next()) {
            allusers.add(new User(rs.getInt("customer_id"), rs.getString("customer_name")));
        }
        showuser(allusers);
    }

    private void showuser(List<User> usersToShow) {
        listvbox.getChildren().clear();
        for (User user : usersToShow) {
            CheckBox check = new CheckBox(user.name);
            check.getStyleClass().add("check-box");
            if (selectedusers.contains(user)) check.setSelected(true);

            check.setOnAction(event -> {
                if (check.isSelected()) {
                    if (selectedusers.size() < userlimit) selectedusers.add(user);
                    else check.setSelected(false);
                } else selectedusers.remove(user);
                updateCheckboxStates();
            });

            HBox box = new HBox(check);
            box.getStyleClass().add("user-item");
            box.setPadding(new Insets(10));
            box.setSpacing(10);
            box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
            listvbox.getChildren().add(box);
        }
    }

    private void updateCheckboxStates() {
        for (Node node : listvbox.getChildren()) {
            if (node instanceof HBox hb) {
                for (Node child : hb.getChildren()) {
                    if (child instanceof CheckBox cb) {
                        cb.setDisable(!cb.isSelected() && selectedusers.size() >= userlimit);
                    }
                }
            }
        }
    }

    @FXML
    private void listconfirmaction(ActionEvent event) throws ClassNotFoundException, SQLException {
        if (selectedusers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a user");
            return;
        }

        List<Integer> userIds = new ArrayList<>();
        for (User user : selectedusers) userIds.add(user.id);

        if (roomcategory.equalsIgnoreCase("general")) {
            String selectedPackage = "Normal";
            int duration = 3600;
            LocalTime now = LocalTime.now();
            String starttime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String pcName = "pc" + pcid;
            for (Integer userid : userIds) {
                insertSale(userid, pcid, roomid, selectedPackage, starttime);
                dashboard.logActivity(pcName+" started session");
                updatePcStatus(pcid);
            }
            
            s.sendToClient("TO|" + pcName + "|UNLOCK|" + pcName + "|" + userIds + "|" + roomid + "|" + selectedPackage + "|" + duration);
            if (roomController != null) {
                roomController.updateCardStatus(pcid, 2);
            }

            
        }
        Stage stage = (Stage) listconfirmbtn.getScene().getWindow();
        stage.close();
    }

    private void insertSale(int userid, int pcid, int roomid, String packageName, String startTime) throws SQLException {
        int packageid = getPackageId(packageName);
        pst = con.prepareStatement("INSERT INTO sale_detail(customer_id, pc_id, room_id, package_id, status_id, start_time, period, sale_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        pst.setInt(1, userid);
        pst.setInt(2, pcid);
        pst.setInt(3, roomid);
        pst.setInt(4, packageid);
        pst.setInt(5, 2);
        pst.setString(6, startTime);
        pst.setInt(7, 1);
        pst.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
        pst.executeUpdate();
    }

    private int getPackageId(String packageName) throws SQLException {
        pst = con.prepareStatement("SELECT package_id FROM package WHERE package_type = ?");
        pst.setString(1, packageName);
        rs = pst.executeQuery();
        return rs.next() ? rs.getInt("package_id") : -1;
    }
    public void setRoomController(RoomController controller) {
        this.roomController = controller;
}

    private void updatePcStatus(int pcid) throws SQLException {
        pst = con.prepareStatement("UPDATE pcs SET status_id = 2 WHERE pc_id = ?");
        pst.setInt(1, pcid);
        pst.executeUpdate();
    }

    public void setlabel(Label label) {
        this.selectedlabel = label;
    }

    @FXML
    private void txtsearchaction(ActionEvent event) {
        String keyword = txtsearch.getText().toLowerCase().trim();
        List<User> filtered = new ArrayList<>();
        for (User user : allusers) {
            if (user.name.toLowerCase().contains(keyword)) {
                filtered.add(user);
            }
        }
        showuser(filtered);
    }

    @FXML
    private void txtsearchkey(KeyEvent event) {
        txtsearchaction(null);
    }

    public List<Integer> getSelectedUserIds() {
        List<Integer> ids = new ArrayList<>();
        for (User user : selectedusers) ids.add(user.id);
        return ids;
    }
    public List<String> getSelectedUserNames() {
    List<String> names = new ArrayList<>();
    for (User user : selectedusers) {
        names.add(user.name);
    }
    return names;
}
}

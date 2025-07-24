package controller;

import database.DbConnection;
import javafx.concurrent.Task;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RoomController implements Initializable {
    @FXML private Button btngeneral;
    @FXML private Button btnprivate;
    @FXML private FlowPane cardcontainer;
    @FXML private AnchorPane roompane;
    @FXML private FlowPane pccontainer;
    @FXML private ScrollPane pcpane;
    @FXML private Button btnaddroom;
    @FXML private Button btnback;
    
    public static RoomController instance;
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;

    private final Map<Integer, PcCardController> pcCardMap = new HashMap<>();
    public Map<String, PcCardController> cardMap = new HashMap<>();
    private String roomtype;
    private String roomcategory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        DbConnection db = new DbConnection();
        try {
            con = db.getConnection();
            loadrooms("general");
             btngeneral.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
        btnprivate.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
            closeResources();
        }
    }

    public void loadrooms(String roomcategory) throws SQLException, IOException {
        closeResources();
        btnback.setVisible(false);
        btnback.setDisable(true);
        roompane.setVisible(true);
        pcpane.setVisible(false);
        cardcontainer.getChildren().clear();
        this.roomcategory = roomcategory;

        String sql = "SELECT * FROM rooms WHERE room_category LIKE ?";
        pst = con.prepareStatement(sql);
        pst.setString(1, roomcategory);
        rs = pst.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("room_id");
            roomtype = rs.getString("room_type");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/roomcard.fxml"));
            AnchorPane card = loader.load();
            RoomCardController cardcontrol = loader.getController();
            cardcontrol.setdata(id, roomtype, this);
            cardcontainer.getChildren().add(card);
        }
    }

 public void loadpcforroom(int roomid) {
    btnback.setVisible(true);
    btnback.setDisable(false);
    pcpane.setVisible(true);
    roompane.setVisible(false);
    pcCardMap.clear();
    pccontainer.getChildren().clear();

    Task<Void> loadTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            Connection taskCon = null;
            PreparedStatement taskPst = null;
            ResultSet taskRs = null;
            
            try {
                // Create new DbConnection instance for the task
                DbConnection db = new DbConnection();
                taskCon = db.getConnection();
                taskPst = taskCon.prepareStatement("SELECT * FROM pcs WHERE room_id = ?");
                taskPst.setInt(1, roomid);
                taskRs = taskPst.executeQuery();

                while (taskRs.next()) {
                    int no = taskRs.getInt("pc_no");
                    String pcname = "PC - " + no;
                    int pcid = taskRs.getInt("pc_id");
                    int statusid = taskRs.getInt("status_id");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pccard.fxml"));
                    AnchorPane card = loader.load();
                    PcCardController cardcontrol = loader.getController();

                    cardcontrol.setRoomType(roomcategory);
                    cardcontrol.setpcinfo(pcname, RoomController.this, roomid, pcid, card, statusid);

                    pcCardMap.put(pcid, cardcontrol);
                    card.setUserData(cardcontrol);

                    Platform.runLater(() -> pccontainer.getChildren().add(card));
                }
            } finally {
                // Close resources in reverse order of creation
                if (taskRs != null) {
                    try { taskRs.close(); } catch (SQLException e) { /* log error */ }
                }
                if (taskPst != null) {
                    try { taskPst.close(); } catch (SQLException e) { /* log error */ }
                }
                if (taskCon != null) {
                    try { taskCon.close(); } catch (SQLException e) { /* log error */ }
                }
            }
            return null;
        }
    };

    new Thread(loadTask).start();
}

    public void updateCardToNormal(int pcId) {
        Platform.runLater(() -> {
            PcCardController cardController = pcCardMap.get(pcId);
            if (cardController != null) {
                AnchorPane cardPane = cardController.card;
                try {
                    cardController.setStatus(1);
                } catch (SQLException ex) {
                    Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    public void updateCardStatus(int pcId, int statusid) throws SQLException {
        PcCardController cardController = pcCardMap.get(pcId);
        if (cardController != null) {
            cardController.setStatus(statusid);
            AnchorPane cardPane = cardController.card;
        } else {
            System.out.println("PC card not found for id: " + pcId);
        }
    }

    public void showuserlist(int pcid, int roomid) throws IOException {
        FXMLLoader userloader = new FXMLLoader(getClass().getResource("/view/userlist.fxml"));
        AnchorPane userpopup = userloader.load();
        UserlistController usercontroller = userloader.getController();
        usercontroller.setroomtype(roomtype, roomcategory);
        usercontroller.setuserinfo2(pcid, roomid);
        usercontroller.setRoomController(this);
        Stage popupstage = new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Select user list");
        popupstage.setScene(new Scene(userpopup));
        popupstage.showAndWait();
    }

    public void showpackages(int pcid, int roomid, AnchorPane card) throws IOException, ClassNotFoundException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/package.fxml"));
        AnchorPane popup = loader.load();

        PackageController controller = loader.getController();
        controller.setpcandroom(pcid, roomid);
        controller.setRoomController(this);
        String roomtype = controller.getroomtype(roomid);
        controller.setroominfo(roomtype, pcid, roomid, roomcategory);

        Stage popupstage = new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Select Package");
        popupstage.setScene(new Scene(popup));
        popupstage.showAndWait();
    }



    @FXML
    private void btngeneralaction(ActionEvent event) throws SQLException, IOException {
        loadrooms("general");
        btngeneral.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
        btnprivate.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
    }

    @FXML
    private void btnprivateaction(ActionEvent event) throws SQLException, IOException {
        loadrooms("private");
        btnprivate.setStyle("-fx-background-color: #ffffff;-fx-border-color: #494949;-fx-text-fill: #141619;-fx-background-radius: 0 10px 0 0;-fx-border-radius: 0 10px 0 0;");
        btngeneral.setStyle("-fx-background-color: #141619;-fx-border-color: #494949;-fx-text-fill: #ffffff;-fx-background-radius: 10px 0 0 0;-fx-border-radius: 10px 0 0 0;");
    }
    
    @FXML
    void btnaddroomaction(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/roomadd.fxml"));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(((Node)event.getSource()).getScene().getWindow());
        Parent root=loader.load();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.initStyle(StageStyle.TRANSPARENT);
        RoomaddController addController = loader.getController();
        //addController.setRoomController(this);
        dialog.setTitle("Add New Room");
        dialog.showAndWait();
    } catch (IOException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Could not open room add dialog");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }        
    }
    
    @FXML
    void btnbackaction(ActionEvent event) {
        if (pcpane.isVisible()) {
        pcpane.setVisible(false);
        roompane.setVisible(true);
        btnback.setVisible(false);
        btnback.setDisable(true);
    }
    }
    
    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public void shutdown() {
        try {
            closeResources();
            if (con != null) con.close();
        } catch (SQLException ex) {
            Logger.getLogger(RoomController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}    

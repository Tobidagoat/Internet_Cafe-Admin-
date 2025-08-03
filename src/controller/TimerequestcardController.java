package controller;

import database.DbConnection;
import internet_cafe_admin.server;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.*;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

public class TimerequestcardController implements Initializable {

    @FXML private Label lbusername;
    @FXML private Label lbextratime;
    @FXML private Button btncancel;
    @FXML private Button btnok;
    @FXML private ImageView lbuserpfp;
    
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    private DashboardController dashboard=DashboardController.getInstance();
    private String id;
    private String clientName;
    private int seconds;
    private String username;
    private String userprofile;
    private AnchorPane parentCard;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DbConnection db = new DbConnection();
        try {
            con = db.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TimerequestcardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Circular profile picture
        Circle clip = new Circle(
            lbuserpfp.getFitWidth()/2,
            lbuserpfp.getFitHeight()/2,
            lbuserpfp.getFitWidth()/2
        );
        lbuserpfp.setClip(clip);
        
        // Setup button effects
        setupButtonEffect(btnok);
        setupButtonEffect(btncancel);
    }
    
    public void setData(String id, String clientName, int seconds) throws SQLException {
        this.id = id;
        this.clientName = clientName;
        this.seconds = seconds;
        getuserinfo(clientName);
        lbusername.setText(username);
        File file = new File("src/img/"+userprofile);
        Image image = new Image(file.toURI().toString());
        lbuserpfp.setImage(image);
        lbextratime.setText("+"+formatTohour(seconds));
        
        
        // Wait until parentCard is set before adding handlers
        if (parentCard != null) {
            setupCardHoverEffects();
        }
    }
    
    public void setParentCard(AnchorPane card) {
        this.parentCard = card;
        card.setUserData(this);
        
        // Now that parentCard is set, we can setup hover effects
        if (clientName != null) { // Make sure data is loaded
            setupCardHoverEffects();
        }
    }
    
    private void setupCardHoverEffects() {
        parentCard.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), parentCard);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            
            DropShadow shadow = new DropShadow(15, Color.rgb(0, 0, 0, 0.2));
            parentCard.setEffect(shadow);
        });
        
        parentCard.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), parentCard);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            parentCard.setEffect(null);
        });
    }

    private void setupButtonEffect(Button btn) {
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
            
            DropShadow glow = new DropShadow(10, Color.rgb(0, 150, 255, 0.7));
            btn.setEffect(glow);
        });
        
        btn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            btn.setEffect(null);
        });
        
        btn.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(50), btn);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
        });
        
        btn.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(100), btn);
            release.setToX(1.0);
            release.setToY(1.0);
            release.play();
        });
    }

    @FXML
    private void btncancelaction(ActionEvent event) {
        animateButtonClick(btncancel, () -> {
             String msg = "Time Request Declined.";
             server.sendToClient("TO|" + clientName + "|CONFIRMATION|" + msg);
             removeCard();
            
                });
    }

    @FXML
    private void btnokaction(ActionEvent event) {
        animateButtonClick(btnok, () -> {
            server.sendToClient("TO|" + clientName + "|ADD_TIME_CONFIRMED|" + seconds);
            System.out.println("Here's the seconds "+seconds);
            if(dashboard!=null){
            dashboard.logActivity("admin added "+formattomin(seconds)+" to "+clientName);}
            
             String msg = "Time Request Accepted.";
             server.sendToClient("TO|" + clientName + "|CONFIRMATION|" + msg);
            
            removeCard();
        });
    }
    
    private void animateButtonClick(Button btn, Runnable action) {
        ScaleTransition click = new ScaleTransition(Duration.millis(100), btn);
        click.setToX(0.9);
        click.setToY(0.9);
        
        ScaleTransition release = new ScaleTransition(Duration.millis(200), btn);
        release.setToX(1.0);
        release.setToY(1.0);
        
        SequentialTransition seq = new SequentialTransition(
            click,
            new PauseTransition(Duration.millis(50)),
            release
        );
        
        seq.setOnFinished(e -> action.run());
        seq.play();
    }
    
    private void removeCard() {
        DashboardController.instance.removeTimeRequestCard(id);
    }
    
    private String formatTohour(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
    private String formattomin(int totalseconds){
        int minutes = (totalseconds / 60);
        return minutes+" mins";
    }
    
    private void getuserinfo(String pcname) throws SQLException {
        int pcid = Integer.parseInt(pcname.replaceAll("[^0-9]", ""));
        String getuserquery = "SELECT customer_name,profile_pic from users where customer_id = "
            + "(SELECT customer_id FROM sale_detail WHERE pc_id= ? AND sale_date = CURRENT_DATE order by sale_id desc limit 1);";
        pst = con.prepareStatement(getuserquery);
        pst.setInt(1, pcid);
        rs = pst.executeQuery();
        while(rs.next()) {
            username = rs.getString("customer_name");
            userprofile = rs.getString("profile_pic");
        }
    }
    
    public String getId() {
        return id;
    }
}
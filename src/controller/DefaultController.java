/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import static internet_cafe_admin.Internet_Cafe_admin.stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.jshell.execution.LoaderDelegate;

/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class DefaultController implements Initializable {

    @FXML
    private ToggleButton btnHome;
    @FXML
    private ToggleGroup sideBarToggleGroup;
    @FXML
    private ToggleButton btnData;
    @FXML
    private ToggleButton btnBooking;
    @FXML
    private ToggleButton btnFood;
    @FXML
    private ToggleButton btnCustomer;
    @FXML
    private ToggleButton btnLogout;
    @FXML
    private ToggleButton btnSetting;
    @FXML
    private AnchorPane mainContentAnchorPane;
    
    @FXML
    private Button btnedit;

    @FXML
    private Label lbdate;

    @FXML
    private Label lbname;

    @FXML
    private Label lbtime;
    
    @FXML
    private ImageView pfp;
    
    @FXML
    private Circle reddot;
    
    private String name;
    private String profile;
    private final BooleanProperty hasNotifications = new SimpleBooleanProperty(false);
    private final BooleanProperty isDashboardActive = new SimpleBooleanProperty(false);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        reddot.visibleProperty().bind(
            hasNotifications.and(isDashboardActive.not())
        );
        sideBarToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
        if (newToggle == null) {
            // Re-select the previous toggle
            sideBarToggleGroup.selectToggle(oldToggle);
        }
    });
        try {
            loadUI("/view/dashboard.fxml",true);
            btnHome.setSelected(true);
            // TODO
        } catch (IOException ex) {
            Logger.getLogger(DefaultController.class.getName()).log(Level.SEVERE, null, ex);
        }
        startClock();
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        lbdate.setText(formattedDate);
        Circle clip = new Circle(
            pfp.getFitWidth()/2,
            pfp.getFitHeight()/2,
            pfp.getFitWidth()/2
        );
        pfp.setClip(clip);
    }    

    @FXML
    private void HandleSwitchHomeAction(ActionEvent event) throws IOException {
        loadUI("/view/dashboard.fxml",true);
    }

    @FXML
    private void HandleSwitchDataAction(ActionEvent event) throws IOException {
       loadUI("/view/data.fxml",false);
    }

    @FXML
    private void HandleSwitchBookingAction(ActionEvent event) throws IOException {
        loadUI("/view/room.fxml",false);
    }

    @FXML
    private void HandleSwitchFoodAction(ActionEvent event) throws IOException {
        loadUI("/view/login.fxml",false);
    }

    @FXML
    private void HandleSwitchCustomerAction(ActionEvent event) throws IOException {
         loadUI("/view/Customer.fxml",false);
    }

    @FXML
    private void HandleSwitchLogoutAction(ActionEvent event) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
    }

    @FXML
    private void HandleSwitchSettingAction(ActionEvent event) {
    }
    
    @FXML
    void btneditaction(ActionEvent event) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile.fxml"));
        AnchorPane popup = loader.load();
        ProfileController controller = loader.getController();
        controller.getname(name);
        Stage popupstage = new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Select Package");
        popupstage.setScene(new Scene(popup));
        popupstage.showAndWait();
    }
    
    private void startClock() {
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm:ss a");
    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            lbtime.setText(LocalTime.now().format(timeFormat));
        }
    };
    timer.start();
}
    
    public void getadmininfo(String name,String profile){
        this.name=name;
        this.profile=profile;
        lbname.setText(name.toUpperCase());
        File file = new File("src/img/"+profile);
        Image image = new Image(file.toURI().toString());
        pfp.setImage(image);
    }
    
    public void setNotification(boolean hasNotification) {
            hasNotifications.set(hasNotification);
    }
    
    public void setActivePane(AnchorPane newPane, boolean isDashboard) {
        mainContentAnchorPane.getChildren().setAll(newPane);
        isDashboardActive.set(isDashboard);
        
        // Clear notification if switching to dashboard
        if (isDashboard) {
            hasNotifications.set(false);
        }
    }
    
    public BooleanProperty hasNotificationsProperty() {
        return hasNotifications;
    }
    
    public void loadUI(String fxmlPath, boolean isDashboard) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        AnchorPane newLoadedPane = loader.load();
        if (isDashboard && loader.getController() instanceof DashboardController) {
            ((DashboardController)loader.getController()).setDefaultController(this);
        }

        setActivePane(newLoadedPane, isDashboard);
        mainContentAnchorPane.getChildren().clear();
        mainContentAnchorPane.getChildren().add(newLoadedPane);
        
        AnchorPane.setTopAnchor(newLoadedPane, 0.0);
        AnchorPane.setLeftAnchor(newLoadedPane, 0.0);
         AnchorPane.setBottomAnchor(newLoadedPane, 0.0);
          AnchorPane.setRightAnchor(newLoadedPane, 0.0);
    }
   
}

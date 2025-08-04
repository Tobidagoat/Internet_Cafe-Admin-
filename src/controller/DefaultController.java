package controller;

import static internet_cafe_admin.Internet_Cafe_admin.stage;
import internet_cafe_admin.server;
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

public class DefaultController implements Initializable {

    @FXML private ToggleButton btnHome;
    @FXML private ToggleGroup sideBarToggleGroup;
    @FXML private ToggleButton btnData;
    @FXML private ToggleButton btnBooking;
    @FXML private ToggleButton btnFood;
    @FXML private ToggleButton btnCustomer;
    @FXML private ToggleButton btnLogout;
    @FXML private ToggleButton btnSetting;
    @FXML private AnchorPane mainContentAnchorPane;
    @FXML private Button btnedit;
    @FXML private Label lbdate;
    @FXML private Label lbname;
    @FXML private Label lbtime;
    @FXML private ImageView pfp;
    @FXML private Circle reddot;
    @FXML private Circle invoicenoti;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnMinimize;
    
    
    private String name;
    private String profile;
    private DashboardController dashboardController;
    private InvoicepaneController invoicepanecontroller;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUIComponents();
        loadInitialView();
        startClock();
        setCurrentDate();
    }

    private void setupUIComponents() {
        sideBarToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                sideBarToggleGroup.selectToggle(oldToggle);
            }
        });

        Circle clip = new Circle(
            pfp.getFitWidth()/2,
            pfp.getFitHeight()/2,
            pfp.getFitWidth()/2
        );
        pfp.setClip(clip);
    }

    private void loadInitialView() {
        try {
            loadUI("/view/dashboard.fxml");
            btnHome.setSelected(true);
        } catch (IOException ex) {
            Logger.getLogger(DefaultController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setCurrentDate() {
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        lbdate.setText(formattedDate);
    }

    @FXML
    private void HandleSwitchHomeAction(ActionEvent event) throws IOException {
        reddot.setVisible(false);
        loadUI("/view/dashboard.fxml");
    }

    @FXML
    private void HandleSwitchDataAction(ActionEvent event) throws IOException {
        loadUI("/view/data.fxml");
    }

    @FXML
    private void HandleSwitchBookingAction(ActionEvent event) throws IOException {
        loadUI("/view/room.fxml");
    }

    @FXML
    private void HandleSwitchFoodAction(ActionEvent event) throws IOException {
        loadUI("/view/food_1.fxml");
    }

    @FXML
    private void HandleSwitchCustomerAction(ActionEvent event) throws IOException {
        loadUI("/view/Customer.fxml");
    }

    @FXML
    private void HandleSwitchLogoutAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
          Parent root = loader.load();
          boolean wasMaximized = stage.isMaximized();

          // Create and set the new scene
          Scene scene = new Scene(root);
          stage.setScene(scene);
          stage.centerOnScreen();

          // If you want to preserve the previous state instead, use:
           stage.setMaximized(wasMaximized);

          stage.show();
    }

    @FXML
    private void HandleSwitchSettingAction(ActionEvent event) throws IOException {
        invoicenoti.setVisible(false);
        loadUI("/view/invoicepane.fxml");
    }
    
    @FXML
    void HandleCloseAction(ActionEvent event) {
        stage.close();
        server.getInstance().stopServer();
    }
    @FXML
    void HandleMiniAction(ActionEvent event) {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
        
    }

    @FXML
    void btneditaction(ActionEvent event) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile.fxml"));
        AnchorPane popup = loader.load();
        ProfileController controller = loader.getController();
        controller.getname(name);
        controller.setProfileUpdateCallback((newName, newProfileImage) -> {
            // Update the main controller's data
            this.name = newName;
            this.profile = newProfileImage;

            // Update the UI immediately
            Platform.runLater(() -> {
                lbname.setText(newName.toUpperCase());
                if (newProfileImage != null && !newProfileImage.isEmpty()) {
                    File file = new File("src/img/" + newProfileImage);
                    if (file.exists()) {
                       Image image = new Image(
                    file.toURI().toString(),
                        550,  
                        550,  
                        true, 
                        true, 
                        true  
                    );
                    pfp.setImage(image);
                    pfp.setPreserveRatio(true);
                    pfp.setSmooth(true);
                    pfp.setCache(true);
                    }
                }
            });
        }); 
        Stage popupstage = new Stage();
        popupstage.initModality(Modality.APPLICATION_MODAL);
        popupstage.setTitle("Edit User");
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

    public void getadmininfo(String name, String profile) {
        this.name = name;
        this.profile = profile;
        lbname.setText(name.toUpperCase());
        File file = new File("src/img/"+profile);
        Image image = new Image(
                    file.toURI().toString(),
                        550,  
                        550,  
                        true, 
                        true, 
                        true  
                    );
                    pfp.setImage(image);
                    pfp.setPreserveRatio(true);
                    pfp.setSmooth(true);
                    pfp.setCache(true);
    }

    public void showNotificationDot() {
        if (!btnHome.isSelected()) {
            reddot.setVisible(true);
        }
    }

    public void hideNotificationDot() {
        reddot.setVisible(false);
    }

    public void showInvoiceNotification() {
        if (!btnSetting.isSelected()) {
            invoicenoti.setVisible(true);
        }
    }

    public void hideInvoiceNotification() {
        invoicenoti.setVisible(false);
    }

    private void loadUI(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        AnchorPane newLoadedPane = loader.load();

        if (fxmlPath.equals("/view/dashboard.fxml")) {
            dashboardController = loader.getController();
            dashboardController.setDefaultController(this);
        }
        else if(fxmlPath.equals("/view/invoicepane.fxml")){
            invoicepanecontroller = loader.getController();
            invoicepanecontroller.setMainController(this);
        }
        else if(fxmlPath.equals("/view/food_1.fxml")){
            FoodController foodcontroller=loader.getController();
            server.getInstance().setFoodController(foodcontroller);
        }

        mainContentAnchorPane.getChildren().clear();
        mainContentAnchorPane.getChildren().add(newLoadedPane);
        
        AnchorPane.setTopAnchor(newLoadedPane, 0.0);
        AnchorPane.setLeftAnchor(newLoadedPane, 0.0);
        AnchorPane.setBottomAnchor(newLoadedPane, 0.0);
        AnchorPane.setRightAnchor(newLoadedPane, 0.0);
    }
}
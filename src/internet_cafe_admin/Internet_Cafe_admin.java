/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML.java to edit this template
 */
package internet_cafe_admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author USER
 */
public class Internet_Cafe_admin extends Application {
    public static Stage stage;
    @Override
    public void start(Stage stage) throws Exception {
        
        this.stage=stage;
         
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED); 
       

        stage.show();
        new Thread(() -> {
            server s = server.getInstance();
             
            s.startServer();
        }).start();
    }

    
    public static void main(String[] args) {
        launch(args);
    }
    
}

package controller;

import database.DbConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class RoomaddController implements Initializable {

    @FXML
    private Button btnadd;
    @FXML
    private ComboBox<String> cbroomcategory;
    @FXML
    private HBox roomtypebox;
    @FXML
    private ComboBox<String> cbroomtype;
    @FXML
    private HBox pcamountbox;
    @FXML
    private TextField txtpcamount;
    
    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        DbConnection db=new DbConnection();
        try {
            con=db.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RoomaddController.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbroomcategory.getItems().addAll("general", "private");
        cbroomtype.getItems().addAll("single", "couple", "team", "squad");
        
        roomtypebox.setVisible(false);
        pcamountbox.setVisible(false);
        
        cbroomcategory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.equals("general")) {
                    pcamountbox.setVisible(true);
                    roomtypebox.setVisible(false);
                    cbroomtype.getSelectionModel().clearSelection();
                } else if (newVal.equals("private")) {
                    roomtypebox.setVisible(true);
                    pcamountbox.setVisible(false);
                    txtpcamount.clear();
                }
            }
        });
        
        cbroomtype.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "single":
                        txtpcamount.setText("1");
                        break;
                    case "couple":
                        txtpcamount.setText("2");
                        break;
                    case "team":
                        txtpcamount.setText("4");
                        break;
                    case "squad":
                        txtpcamount.setText("8");
                        break;
                }
            }
        });
        txtpcamount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtpcamount.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }    

    @FXML
    private void btncloseaction(MouseEvent event) {
        ((Stage) btnadd.getScene().getWindow()).close();
    }

    @FXML
    private void btnaddaction(ActionEvent event) throws SQLException {
        String category = cbroomcategory.getValue();
        String type = cbroomtype.getValue();
        String pcAmountText = txtpcamount.getText();
        
        if (category == null) {
            System.out.println("Please select a room category");
            return;
        }
        
        if (category.equals("private") && type == null) {
            System.out.println("Please select a room type");
            return;
        }
        
        if (pcAmountText.isEmpty()) {
            System.out.println("PC amount cannot be empty");
            return;
        }
        
        int pcAmount;
        try {
            pcAmount = Integer.parseInt(pcAmountText);
        } catch (NumberFormatException e) {
            System.out.println("Invalid PC amount");
            return;
        }
        
        if(type == null) insertRoom(category, "general", pcAmount);
        else insertRoom(category, type, pcAmount);
        
        ((Stage) btnadd.getScene().getWindow()).close();
    }
    
    private void insertRoom(String category, String type, int pcAmount) throws SQLException {
        String sql="INSERT into rooms (room_category,room_type,total_pc) VALUES (?, ?, ?)";
        pst=con.prepareStatement(sql);
        pst.setString(1, category);
        pst.setString(2, type);
        pst.setInt(3, pcAmount);
        pst.executeUpdate();
        
        System.out.println("Inserting room:");
        System.out.println("Category: " + category);
        System.out.println("Type: " + type);
        System.out.println("PC Amount: " + pcAmount);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import database.DbConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class DataController implements Initializable {

    @FXML
    private ToggleButton btn1week;
    @FXML
    private ToggleGroup date_switch_group;
    @FXML
    private ToggleButton btn1month;
    @FXML
    private ToggleButton btn3month;
    @FXML
    private ToggleButton btnAllTime;
    @FXML
    private AreaChart<Number, Number> areaChart;
    @FXML 
    private NumberAxis xAxis;
    @FXML 
    private NumberAxis yAxis;
    @FXML
    private PieChart pieChart;
    
    //AreaChart elements
    
    private XYChart.Series<Number, Number> gamingIncomeSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> foodIncomeSeries = new XYChart.Series<>();
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
     
     
     
     
    
    
    Connection con;
    Statement st;
    ResultSet rs;
    PreparedStatement pst;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        DbConnection db = new DbConnection();
        try {
            con = db.getConnection();
            // TODO
        } catch (ClassNotFoundException ex) {
            System.out.println("db not connected");
        }
        
        //Area Chart Set-up
        
        areaChart.setTitle("Sale Information");
        areaChart.getData().addAll(gamingIncomeSeries, foodIncomeSeries);
        
        
        gamingIncomeSeries.setName("Internet cafe Income");
        foodIncomeSeries.setName("Food Income");
        
        //Calling this month
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
        long lowerBound = startOfMonth.toEpochDay();
        long upperBound = endOfMonth.toEpochDay();

        
        xAxis.setTickUnit(1); // 1 day
        xAxis.setMinorTickCount(0); // No half-days or fractions
        xAxis.setForceZeroInRange(false); // avoid left side hug
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        
        
        
        //Formatting Date
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return LocalDate.ofEpochDay(object.longValue()).format(formatter);  // Example: "Jul 9"
        }

        @Override
        public Number fromString(String string) {
            return LocalDate.parse(string, formatter).toEpochDay();
    }
});



        
        //Real-time update
       Timeline timeline = new Timeline(
        new KeyFrame(Duration.ZERO, e -> {
            try {
                updateChartData();
            } catch (SQLException ex) {
                 System.out.println("db not connected in time frame ");
                
            }
        }),          // 👉 fire immediately!
        new KeyFrame(Duration.seconds(5))                         // 👉 then wait 5 sec
        );


        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
   
        
    }    

    @FXML
    private void Handle1weekAction(ActionEvent event) {
    }

    @FXML
    private void Handle1monthAction(ActionEvent event) {
    }

    @FXML
    private void Handle3monthAction(ActionEvent event) {
    }

    @FXML
    private void HandleAllTimeAction(ActionEvent event) {
    }

    private void updateChartData() throws SQLException {
        
        
        
        String sql ="select sale.sale_date, total_price, total_food_price from sale, food_order where sale.sale_date = food_order.sale_date order by sale.sale_date ASC;";
        st = con.prepareStatement(sql);
        rs = st.executeQuery(sql);
        
         while (rs.next()) {
                
                String dateStr =  rs.getString("sale_date");
                LocalDate date = LocalDate.parse(dateStr, formatter);
                long epochDay = date.toEpochDay(); // convert to numeric X value

                
                    double gamingIncome = rs.getDouble("total_price");
                    double foodIncome = rs.getDouble("total_food_price");

                    Platform.runLater(() -> {
                        gamingIncomeSeries.getData().add(new XYChart.Data<>(epochDay, gamingIncome));
                       foodIncomeSeries.getData().add(new XYChart.Data<>(epochDay, foodIncome));
                        areaChart.requestLayout();


                    });

                    
                
         }
    }
    
   
}

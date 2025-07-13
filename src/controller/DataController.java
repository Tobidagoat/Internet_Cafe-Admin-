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
import java.util.ResourceBundle;
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
import javafx.geometry.Side;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Linn Hein Htet
 */
public class DataController implements Initializable {

    @FXML
    private Button btn1month;

    @FXML
    private Button btn1week;

    @FXML
    private Button btn3month;

    @FXML
    private Button btnAllTime;
    @FXML
    private AreaChart<Number, Number> areaChart;
    @FXML 
    private NumberAxis xAxis;
    @FXML 
    private NumberAxis yAxis;
    @FXML
    private PieChart pieChart;
    @FXML
    private PieChart pieChart2;

    
    private Button selectedToggle = null;
   
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
            
        } catch (ClassNotFoundException ex) {
            System.out.println("db not connected");
        }
        
        //Area Chart Set-up
        
        areaChart.setTitle("Sale Information");
        areaChart.getData().addAll(gamingIncomeSeries, foodIncomeSeries);
        gamingIncomeSeries.setName("Internet cafe Income");
        foodIncomeSeries.setName("Food Income");

        btn1month.fire();
         
        xAxis.setMinorTickCount(0); // No half-days or fractions
        xAxis.setForceZeroInRange(false); // avoid left side hug
        xAxis.setAutoRanging(false);
        
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
        }),     
        new KeyFrame(Duration.seconds(5)) 
        );


        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        
        //Pie Chart for food
        
        pieChart.setData(getPieChartData());
        pieChart.setTitle("Best Sold Top-5 Food&Drink");
        pieChart.setVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setPrefWidth(450); // Limit the chart width

        
        //Pie Chart for package
        pieChart2.setData(getPieChart2Data());
        pieChart2.setTitle("Best Sold Packages");
        pieChart2.setVisible(true);
        pieChart2.setLabelsVisible(true);
        pieChart2.setLegendSide(Side.RIGHT);
        pieChart2.setPrefWidth(450); // Limit the chart width

         
        
   
        
    }    

     @FXML
    void HandleToggle(ActionEvent event) {

        Button clicked = (Button) event.getSource();
    setToggle(clicked); 
    }
    
    private void setToggle(Button btn) {
    if (selectedToggle != null) {
        selectedToggle.getStyleClass().remove("selected-toggle");
    }

    selectedToggle = btn;
    selectedToggle.getStyleClass().add("selected-toggle");

//    System.out.println("Selected: " + btn.getText());

    // Trigger custom logic
    if (btn == btn1week) {
        call1week();
    } else if (btn == btn1month) {
        call1month();
    } else if (btn == btn3month) {
        call3month();
    }else if(btn==btnAllTime){
        try {
            callAllTime();
        } catch (SQLException ex) {
            System.out.println("Error alll time");
        }
    }
}

    private void updateChartData() throws SQLException {
  
        String sql ="SELECT d.sale_date, m.total_main AS main_income, f.total_food AS food_income FROM ( SELECT sale_date FROM sale UNION SELECT sale_date FROM food_order ) d LEFT JOIN ( SELECT sale_date, SUM(total_price) AS total_main FROM sale GROUP BY sale_date ) m ON d.sale_date = m.sale_date LEFT JOIN ( SELECT sale_date, SUM(total_food_price) AS total_food FROM food_order GROUP BY sale_date) f ON d.sale_date = f.sale_date ORDER BY d.sale_date ASC;";
        st = con.prepareStatement(sql);
        rs = st.executeQuery(sql);
        
         while (rs.next()) {
                
                String dateStr =  rs.getString("sale_date");
                LocalDate date = LocalDate.parse(dateStr, formatter);
                long epochDay = date.toEpochDay(); // convert to numeric X value

                    double gamingIncome = rs.getDouble("main_income");
                    double foodIncome = rs.getDouble("food_income");

                    Platform.runLater(() -> {
                        gamingIncomeSeries.getData().add(new XYChart.Data<>(epochDay, gamingIncome));
                       foodIncomeSeries.getData().add(new XYChart.Data<>(epochDay, foodIncome));
                        areaChart.requestLayout();
                    });
         }
    }
    public void call1week(){
        
    }
    public void call1month(){
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
        long lowerBound = startOfMonth.toEpochDay();
        long upperBound = endOfMonth.toEpochDay();
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setTickUnit(1);
    }
    public void call3month(){
         LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.minusMonths(2).withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
        long lowerBound = startOfMonth.toEpochDay();
        long upperBound = endOfMonth.toEpochDay();
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setTickUnit(10);
        
    }
    public void callAllTime() throws SQLException{
        String sql = "select sale_date from sale  ORDER BY sale_date ASC limit 1;";
        st = con.prepareStatement(sql);
        rs =st.executeQuery(sql);
        String date=null;
        while(rs.next()){
             date = rs.getString("sale_date"); 
             
        }
         LocalDate today = LocalDate.now();
         LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
         LocalDate startday =LocalDate.parse(date);
         
         
        long lowerBound = startday.toEpochDay();
        long upperBound = endOfMonth.toEpochDay();
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setTickUnit(10);
         
        
    }
    public ObservableList<PieChart.Data> getPieChartData() {
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    int total_sold_of_top_5 = 0;
    
    // Get total sold of top 5
    String sqll = "SELECT SUM(total_sold) AS top5_total_qty FROM (SELECT SUM(food_order_detail.qty) AS total_sold FROM food_order_detail JOIN foods ON foods.food_id = food_order_detail.food_id GROUP BY foods.food_id ORDER BY total_sold DESC LIMIT 5) AS top5;";
    try {
        st = con.prepareStatement(sqll);
        rs = st.executeQuery(sqll);
        
        if(rs.next()) {
            total_sold_of_top_5 = rs.getInt("top5_total_qty");
        }
    } catch (SQLException ex) {
        System.out.println("pie chart data pull error: total sum of top 5");
        ex.printStackTrace();
        return pieChartData; // return empty list if error
    }
    
    // Get individual food data
    String sql = "SELECT foods.food_name as famous_food, SUM(food_order_detail.qty) AS total_sold FROM food_order_detail JOIN foods ON foods.food_id = food_order_detail.food_id GROUP BY foods.food_id, foods.food_name ORDER BY total_sold DESC LIMIT 5;";
    try {
        st = con.prepareStatement(sql);
        rs = st.executeQuery(sql);
        
        while(rs.next()) {
            int total_sold = rs.getInt("total_sold");
            // Cast to double before division to avoid integer division
            double percentage = ((double)total_sold / total_sold_of_top_5) * 100;
            pieChartData.add(new PieChart.Data(rs.getString("famous_food"), percentage));
        }
    } catch (SQLException ex) {
        System.out.println("pie chart data pull error");
        ex.printStackTrace();
    }
    
    return pieChartData;
}
    
    public ObservableList<PieChart.Data> getPieChart2Data(){
    ObservableList<PieChart.Data> pieChart2Data = FXCollections.observableArrayList();
    int total_sold_period=0;
    
     String sqll = "select sum(period) as total_period from sale_detail;";
        try {
        st = con.prepareStatement(sqll);
        rs = st.executeQuery(sqll);
        
        if(rs.next()) {
            total_sold_period= rs.getInt("total_period");
        }
    } catch (SQLException ex) {
        System.out.println("pie chart data pull error: total period for piechart2");
        ex.printStackTrace();
        return pieChart2Data; // return empty list if error
    }
    
    String sql ="SELECT p.package_id, p.package_type as name, SUM(sd.period) AS total_period FROM sale_detail sd JOIN package p ON sd.package_id = p.package_id GROUP BY p.package_id, p.package_type ORDER BY total_period DESC;";
    
    try {
        st = con.prepareStatement(sql);
        rs = st.executeQuery(sql);
        
        while(rs.next()) {
           int total_period_for_package = rs.getInt("total_period");
            // Cast to double before division to avoid integer division
            double percentage = ((double)total_period_for_package / total_sold_period) * 100;
            
            pieChart2Data.add(new PieChart.Data(rs.getString("name"), percentage));
        }
    } catch (SQLException ex) {
        System.out.println("pie chart 2 data pull error");
        ex.printStackTrace();
    }
    
    return pieChart2Data;
        
    }
    
  


    
   
}

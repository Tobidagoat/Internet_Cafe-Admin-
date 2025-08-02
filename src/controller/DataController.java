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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Map;
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
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
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
    public AreaChart<Number, Number> areaChart;
    @FXML 
    private NumberAxis xAxis;
    @FXML 
    private NumberAxis yAxis;
    @FXML
    private PieChart pieChart;
    @FXML
    private PieChart pieChart2;
     @FXML
    private Label txtFoodIncome;

    @FXML
    private Label txtMainIncome;

    @FXML
    private Label txtTotalIncome;
   
    @FXML
    private Circle cir1;

    @FXML
    private Circle cir2;

    
    private Button selectedToggle = null;
    
    private Timeline chartTimeline;
    
    boolean circheck = false;

   
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
        btn1month.fire();

        
        //Pie Chart for food
        
        pieChart.setData(getPieChartData());
        pieChart.setTitle("Best Sold Top-5 Food&Drink");
        pieChart.setVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setPrefWidth(450); 

        
        //Pie Chart for package
        pieChart2.setData(getPieChart2Data());
        pieChart2.setTitle("Best Sold Packages");
        pieChart2.setVisible(true);
        pieChart2.setLabelsVisible(true);
        pieChart2.setLegendSide(Side.RIGHT);
        pieChart2.setPrefWidth(450); 
        
        
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
   String sql = "SELECT d.sale_date as week_start, m.total_main AS main_income, f.total_food AS food_income FROM ( SELECT sale_date FROM sale UNION SELECT sale_date FROM food_order ) d LEFT JOIN ( SELECT sale_date, SUM(total_price) AS total_main FROM sale GROUP BY sale_date ) m ON d.sale_date = m.sale_date LEFT JOIN ( SELECT sale_date, SUM(total_food_price) AS total_food FROM food_order GROUP BY sale_date) f ON d.sale_date = f.sale_date ORDER BY d.sale_date ASC;";
       


    PreparedStatement st = con.prepareStatement(sql);
    ResultSet rs = st.executeQuery();

    // ✅ Create fresh series
    XYChart.Series<Number, Number> gamingIncomeSeries = new XYChart.Series<>();
    XYChart.Series<Number, Number> foodIncomeSeries = new XYChart.Series<>();
//    gamingIncomeSeries.setName("Internet Cafe Income");
//    foodIncomeSeries.setName("Food Income");

    while (rs.next()) {
        LocalDate date = rs.getDate("week_start").toLocalDate();
        long epochDay = date.toEpochDay();

        double gamingIncome = rs.getDouble("main_income");
        double foodIncome = rs.getDouble("food_income");

        gamingIncomeSeries.getData().add(new XYChart.Data<>(epochDay, gamingIncome));
        foodIncomeSeries.getData().add(new XYChart.Data<>(epochDay, foodIncome));
    }

    rs.close();
    st.close();

    // ✅ Safely update chart on JavaFX thread
    Platform.runLater(() -> {
        areaChart.getData().clear(); // remove old series
        areaChart.getData().addAll(gamingIncomeSeries, foodIncomeSeries); // add new ones
        cirColor();



    });
}

    public void call1week(){
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
         CreateAreaChart(startOfWeek, endOfWeek, 1, Granularity.DAILY);
         
         try {
            //Total income
            setTotalIncome(Granularity.oneweek);
        } catch (SQLException ex) {
            System.out.println("error from init settotalincome");
        }
         
          try {
            //Main income
            setMainIncome(Granularity.oneweek);
        } catch (SQLException ex) {
            System.out.println("set main income error");
        }
          
           try {
            //Food income
            setFoodIncome(Granularity.oneweek);
        } catch (SQLException ex) {
            System.out.println("set food income error");
        }

        
    }
    public void call1month(){
        
        
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
        CreateAreaChart(startOfMonth, endOfMonth, 1, Granularity.DAILY);
        
        try {
            //Total income
            setTotalIncome(Granularity.onemonth);
        } catch (SQLException ex) {
            System.out.println("error from init settotalincome");
        }
        
        try {
            //Main income
            setMainIncome(Granularity.onemonth);
        } catch (SQLException ex) {
            System.out.println("set main income error");
        }
        
         try {
            //Food income
            setFoodIncome(Granularity.onemonth);
        } catch (SQLException ex) {
            System.out.println("set food income error");
        }

        
        
    }
    public void call3month(){
         LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.minusMonths(2).withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
         CreateAreaChart(startOfMonth, endOfMonth, 3, Granularity.WEEKLY);
         
         try {
            //Total income
            setTotalIncome(Granularity.threemonth);
        } catch (SQLException ex) {
            System.out.println("error from init settotalincome");
        }
         
         try {
            //Main income
            setMainIncome(Granularity.threemonth);
        } catch (SQLException ex) {
            System.out.println("set main income error");
        }
         
          try {
            //Food income
            setFoodIncome(Granularity.threemonth);
        } catch (SQLException ex) {
            System.out.println("set food income error");
        }
         
        

        
    }
    public void callAllTime() throws SQLException{
        String sql = "select Min(sale_date) as sale_date from sale";
        st = con.prepareStatement(sql);
        rs =st.executeQuery(sql);
        LocalDate startday=null;
        while(rs.next()){
              startday = rs.getDate("sale_date").toLocalDate();
             
        }
         LocalDate today = LocalDate.now();
         LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
         
          CreateAreaChart(startday, endOfMonth, 3, Granularity.WEEKLY);
          
          try {
            //Total income
            setTotalIncome(Granularity.alltime);
        } catch (SQLException ex) {
            System.out.println("error from init settotalincome");
        }
          
          try {
            //Main income
            setMainIncome(Granularity.alltime);
        } catch (SQLException ex) {
            System.out.println("set main income error");
        }
          
           try {
            //Food income
            setFoodIncome(Granularity.alltime);
        } catch (SQLException ex) {
            System.out.println("set food income error");
        }
         
         
        
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

            double percentage = ((double)total_period_for_package / total_sold_period) * 100;
            
            pieChart2Data.add(new PieChart.Data(rs.getString("name"), percentage));
        }
    } catch (SQLException ex) {
        System.out.println("pie chart 2 data pull error");
        ex.printStackTrace();
    }
    
    return pieChart2Data;
        
    }
    
    
    public enum Granularity {
    DAILY, WEEKLY, MONTHLY , oneweek, onemonth,threemonth, alltime
}

    public void CreateAreaChart(LocalDate from, LocalDate to, int tickUnit, Granularity granularity) {
        
    areaChart.getData().clear(); 

    // Reset axis
    areaChart.setTitle("Sale Information");
    long lowerBound = from.toEpochDay();
    long upperBound = to.toEpochDay();
    xAxis.setLowerBound(lowerBound);
    xAxis.setUpperBound(upperBound);
    xAxis.setTickUnit(tickUnit);
    xAxis.setMinorTickCount(0);
    xAxis.setForceZeroInRange(false);
    xAxis.setAutoRanging(false);

    // Format labels
    xAxis.setTickLabelFormatter(new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return LocalDate.ofEpochDay(object.longValue()).format(formatter);
        }
        @Override
        public Number fromString(String string) {
            return LocalDate.parse(string, formatter).toEpochDay();
        }
    });
    
        try {
            //    if (chartTimeline != null) {
//    chartTimeline.stop();
//    chartTimeline = null;
//}
//
//    
//    Timeline charTimeline = new Timeline(
//        new KeyFrame(Duration.ZERO, e -> {
//            try {
//                 updateChartData(granularity);
//                
//            } catch (SQLException ex) {
//                System.out.println("DB not connected in time frame");
//            }
//        }),
//        new KeyFrame(Duration.seconds(5))
//    );
//    charTimeline.setCycleCount(Timeline.INDEFINITE);
//    charTimeline.play();


updateChartData();
        } catch (SQLException ex) {
            System.out.println("Update area chart data error");
        }
    }
        
//        public void setTotalIncome(){
//            int total =0;
//        try {
//            String sql = "SELECT (SELECT SUM(total_price) FROM sale) + (SELECT SUM(total_food_price) FROM food_order) AS total_income;";
//            st = con.prepareStatement(sql);
//            rs =st.executeQuery(sql);
//            if(rs.next())
//                 total = rs.getInt("total_income");
//            txtTotalIncome.setText(Integer.toString(total));
//            
//        } catch (SQLException ex) {
//            Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("total income error");
//        }
//        }
    
    public void setTotalIncome(Granularity granularity) throws SQLException {
        int total =0;
    LocalDate today = LocalDate.now();
    LocalDate startDate = null;
    switch (granularity) {
        case oneweek -> startDate =today.minusWeeks(1);
        case onemonth -> startDate =today.minusMonths(1);
        case threemonth ->startDate = today.minusMonths(3);
        case alltime ->startDate =null;
    };

    String sql;
    if (startDate != null) {
        sql = """
              SELECT
                  COALESCE((SELECT SUM(total_price) FROM sale WHERE sale_date >= ?), 0) +
                  COALESCE((SELECT SUM(total_food_price) FROM food_order WHERE sale_date >= ?), 0)
              AS total_income
              """;
    } else {
        sql = """
              SELECT
                  COALESCE((SELECT SUM(total_price) FROM sale), 0) +
                  COALESCE((SELECT SUM(total_food_price) FROM food_order), 0)
              AS total_income
              """;
    }

    pst = con.prepareStatement(sql);
        if (startDate != null) {
            Date sqlDate = java.sql.Date.valueOf(startDate);;
            pst.setDate(1, (java.sql.Date) sqlDate);
            pst.setDate(2, (java.sql.Date) sqlDate);
        }

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            total = rs.getInt("total_income");
            
            
        }
        txtTotalIncome.setText(Integer.toString(total));
    

   
}

    
    public void setMainIncome(Granularity granularity) throws SQLException{
    int total =0;
    LocalDate today = LocalDate.now();
    LocalDate startDate = null;
    switch (granularity) {
        case oneweek -> startDate =today.minusWeeks(1);
        case onemonth -> startDate =today.minusMonths(1);
        case threemonth ->startDate = today.minusMonths(3);
        case alltime ->startDate =null;
    };

    String sql;
    if (startDate != null) {
        sql = "SELECT COALESCE(SUM(total_price), 0) AS main_income FROM sale WHERE sale_date >= ?";
    } else {
        sql = "SELECT COALESCE(SUM(total_price), 0) AS main_income FROM sale";
    }

    pst = con.prepareStatement(sql);
        if (startDate != null) {
            Date sqlDate = java.sql.Date.valueOf(startDate);
            pst.setDate(1, (java.sql.Date) sqlDate);
           
        }

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            total = rs.getInt("main_income");
            
            
        }
        txtMainIncome.setText(Integer.toString(total));
            }
        
        
       public void setFoodIncome(Granularity granularity) throws SQLException{
    int total =0;
    LocalDate today = LocalDate.now();
    LocalDate startDate = null;
    switch (granularity) {
        case oneweek -> startDate =today.minusWeeks(1);
        case onemonth -> startDate =today.minusMonths(1);
        case threemonth ->startDate = today.minusMonths(3);
        case alltime ->startDate =null;
    };

    String sql;
    if (startDate != null) {
        sql = "SELECT COALESCE(SUM(total_food_price), 0) AS food_income FROM food_order WHERE sale_date >= ?";
    } else {
        sql = "SELECT COALESCE(SUM(total_food_price), 0) AS food_income FROM food_order";
    }

    pst = con.prepareStatement(sql);
        if (startDate != null) {
            Date sqlDate = java.sql.Date.valueOf(startDate);
            pst.setDate(1, (java.sql.Date) sqlDate);
           
        }

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            total = rs.getInt("food_income");
            
            
        }
        txtFoodIncome.setText(Integer.toString(total));
            }



    
  private void cirColor(){
      if (circheck == false){
         
          
          cir1.setStyle("-fx-fill : orange;");
          cir2.setStyle("-fx-fill: yellow;");
          circheck = true;
      }
      else{
           cir1.setStyle("-fx-fill : green;");
          cir2.setStyle("-fx-fill: blue;");
          circheck = false;
      }
  }


    
   
}

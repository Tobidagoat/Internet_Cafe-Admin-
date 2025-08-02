package controller;

import controller.DataController.Granularity;
import static controller.DataController.Granularity.DAILY;
import database.DbConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class DashboardController implements Initializable {

    @FXML private Label lbactivepc, lbpcratio, lbtodaysale, lbtodaysalecompare, lbactivesession, lbnewuser, lbnewusercompare;
    @FXML private AreaChart<Number, Number> chart;
    @FXML private NumberAxis xAxis;
    @FXML private HBox timerequestcontainer;
    @FXML private ListView<String> activityLogListView;
    
    private DefaultController defaultController;
    private static final ObservableList<String> activityLogs = FXCollections.observableArrayList();
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    public static DashboardController instance;
    private Timeline refreshTimer;
    private int todaytotalrevenue, yestotalrevenue, new_users, yes_users;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<TimeRequest> timeRequestList = new ArrayList<>();

    // Animation properties
    private final Duration CARD_ANIM_DURATION = Duration.millis(350);
    private final Interpolator ANIM_INTERPOLATOR = Interpolator.SPLINE(0.25, 0.1, 0.25, 1);

    public class TimeRequest {
        String id;
        String clientName;
        int seconds;

        public TimeRequest(String clientName, int seconds) {
            this.id = UUID.randomUUID().toString();
            this.clientName = clientName;
            this.seconds = seconds;
        }
    }
    
    public static DashboardController getInstance() {
        return instance;
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        DbConnection db = new DbConnection();
        try {
            con = db.getConnection();
            setupAutoRefresh();
            getactivepc();
            getactivesession();
            gettodayrevenue();
            getuser();
            setupchart();
            
            // Enable hardware acceleration for animations
            timerequestcontainer.setCache(true);
            timerequestcontainer.setCacheHint(CacheHint.SPEED);
            
            activityLogListView.setItems(activityLogs);
            activityLogListView.scrollTo(activityLogs.size() - 1);
            reloadTimeRequestCards();
            activityLogListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item)); // Show full message on hover
                    setStyle("-fx-font-size: 12px; -fx-wrap-text: true;");
                }
            }
        });
        } catch (Exception ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setupAutoRefresh() {
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            try {
                getactivepc();
                getactivesession();
                gettodayrevenue();
                getuser();
            } catch (SQLException e) {
                lbactivepc.setText("Error");
                lbpcratio.setText("");
                e.printStackTrace();
            }
        }));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
        refreshTimer.play();
    }
    

    public void logActivity(String message) {
        Platform.runLater(() -> {
            // 1. Embend timestamp cuz why not
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("[h:mm a]"));
            String logEntry = "["+timestamp + "] " + message;
            
            // 2. Add to the top (or bottom) of da list
            activityLogs.add(logEntry); // For bottom: add(0, logEntry) for top
            
            // 3. Auto-scroll to the newest entry
            activityLogListView.scrollTo(activityLogs.size() - 1);
            
            // 4. Limit log size (e.g., last 10 messages) and fade out da oldest
            if (activityLogs.size() > 10) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), 
                    activityLogListView.lookup(".list-cell")); // Targets the oldest cell
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> activityLogs.remove(0));
                fadeOut.play();
            }
        });
    }

    private void getactivepc() throws SQLException {
        String query = "SELECT SUM(status_id = 1) AS active_pcs, COUNT(*) AS total_pcs FROM pcs";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            int active = rs.getInt("active_pcs");
            int total = rs.getInt("total_pcs");
            lbactivepc.setText(String.format("%d/%d", active, total));
            double ril = total > 0 ? (active * 100.0) / total : 0.0;
            double unoccupied = 100 - ril;
            if(unoccupied!=0){
            lbpcratio.setText(String.format("%.2f%% occupied pcs", unoccupied));
            lbpcratio.setStyle(unoccupied > 50 ? "-fx-text-fill: #ff8175;" : "-fx-text-fill: #6cc95c;");
            }else{
                lbpcratio.setText("");
            }
        }
    }

    private void getactivesession() throws SQLException {
        pst = con.prepareStatement("SELECT SUM(status_id = 2) AS active_sessions FROM sale_detail");
        rs = pst.executeQuery();
        if (rs.next()) {
            lbactivesession.setText(rs.getInt("active_sessions") + " sessions");
        }
    }

    private void gettodayrevenue() throws SQLException {
        String today = "SELECT SUM(combined.amount) AS total_daily_income FROM (SELECT total_price AS amount FROM sale WHERE sale_date = CURRENT_DATE UNION ALL SELECT total_food_price AS amount FROM food_order WHERE sale_date = CURRENT_DATE) AS combined";
        pst = con.prepareStatement(today);
        rs = pst.executeQuery();
        if (rs.next()) {
            todaytotalrevenue = rs.getInt("total_daily_income");
            lbtodaysale.setText(todaytotalrevenue + " KS");
        }

        String yesterday = "SELECT SUM(combined.amount) AS yesterdays_total_income FROM (SELECT total_price AS amount FROM sale WHERE sale_date = CURRENT_DATE - INTERVAL 1 DAY UNION ALL SELECT total_food_price AS amount FROM food_order WHERE sale_date = CURRENT_DATE - INTERVAL 1 DAY) AS combined;";
        pst = con.prepareStatement(yesterday);
        rs = pst.executeQuery();
        if (rs.next()) yestotalrevenue = rs.getInt("yesterdays_total_income");

        int diff = todaytotalrevenue - yestotalrevenue;
        if (diff != 0) {
            lbtodaysalecompare.setText((diff > 0 ? "+" : "") + diff + " from yesterday");
            lbtodaysalecompare.setStyle("-fx-text-fill: " + (diff > 0 ? "#6cc95c" : "#282828") + ";");
        } else lbtodaysalecompare.setText("");
    }

    private void getuser() throws SQLException {
        pst = con.prepareStatement("SELECT COUNT(*) AS user_count FROM users WHERE DATE(date) = CURRENT_DATE;");
        rs = pst.executeQuery();
        if (rs.next()) {
            new_users = rs.getInt("user_count");
            if(new_users>0)
            lbnewuser.setText("+" + new_users + " New users");
        }

        pst = con.prepareStatement("SELECT COUNT(*) AS user_count FROM users WHERE DATE(date) = CURRENT_DATE - INTERVAL 1 DAY;");
        rs = pst.executeQuery();
        if (rs.next()) yes_users = rs.getInt("user_count");

        int diff = new_users - yes_users;
        if (diff != 0) {
            lbnewusercompare.setText((diff > 0 ? "+" : "") + diff + " from yesterday");
            lbnewusercompare.setStyle("-fx-text-fill: " + (diff > 0 ? "#6cc95c" : "#ff8175") + ";");
        } else lbnewusercompare.setText("");
    }

    private void setupchart() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        xAxis.setLowerBound(start.toEpochDay());
        xAxis.setUpperBound(end.toEpochDay());
        xAxis.setTickUnit(1);
        xAxis.setAutoRanging(false);
        chart.setLegendVisible(false);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
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
            updateChartData(DAILY);
        } catch (SQLException ex) {
            System.out.println("Update chart error");
        }
    }

    private void updateChartData(Granularity granularity) throws SQLException {
        String sql = (granularity == DAILY) ?
            "SELECT d.sale_date as week_start, m.total_main AS main_income, f.total_food AS food_income FROM ( SELECT sale_date FROM sale UNION SELECT sale_date FROM food_order ) d LEFT JOIN ( SELECT sale_date, SUM(total_price) AS total_main FROM sale GROUP BY sale_date ) m ON d.sale_date = m.sale_date LEFT JOIN ( SELECT sale_date, SUM(total_food_price) AS total_food FROM food_order GROUP BY sale_date) f ON d.sale_date = f.sale_date ORDER BY d.sale_date ASC;"
            :
            "SELECT STR_TO_DATE(CONCAT(YEARWEEK(d.sale_date, 1), ' Monday'), '%X%V %W') AS week_start, SUM(m.total_main) AS main_income, SUM(f.total_food) AS food_income FROM ( SELECT DISTINCT sale_date FROM sale UNION SELECT DISTINCT sale_date FROM food_order ) d LEFT JOIN ( SELECT sale_date, SUM(total_price) AS total_main FROM sale GROUP BY sale_date ) m ON d.sale_date = m.sale_date LEFT JOIN ( SELECT sale_date, SUM(total_food_price) AS total_food FROM food_order GROUP BY sale_date ) f ON d.sale_date = f.sale_date GROUP BY YEARWEEK(d.sale_date, 1) ORDER BY week_start ASC";

        PreparedStatement st = con.prepareStatement(sql);
        ResultSet rs = st.executeQuery();

        XYChart.Series<Number, Number> gamingSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>();

        while (rs.next()) {
            long epoch = rs.getDate("week_start").toLocalDate().toEpochDay();
            gamingSeries.getData().add(new XYChart.Data<>(epoch, rs.getDouble("main_income")));
            foodSeries.getData().add(new XYChart.Data<>(epoch, rs.getDouble("food_income")));
        }
        rs.close(); st.close();

        Platform.runLater(() -> {
            chart.getData().clear();
            chart.getData().addAll(gamingSeries, foodSeries);
        });
    }
    
    public void setDefaultController(DefaultController defaultController) {
        this.defaultController = defaultController;
    }
    
    public void triggerNotificationEvent() {
    Platform.runLater(() -> {
        if (defaultController != null) {
            defaultController.showNotificationDot();
        }
    });
    }

    public void addTimeRequestCard(String clientName, int seconds) throws SQLException {
        TimeRequest request = new TimeRequest(clientName, seconds);
        timeRequestList.add(request);
        logActivity(clientName+" requested +"+formattomin(seconds));
        triggerNotificationEvent();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/timerequestcard.fxml"));
            AnchorPane card = loader.load();
            TimerequestcardController controller = loader.getController();
            controller.setData(request.id, request.clientName, request.seconds);
            controller.setParentCard(card);
            triggerNotificationEvent();
            // Entrance animation
            card.setOpacity(0);
            card.setTranslateX(50);
            
            ParallelTransition pt = new ParallelTransition(
                new TranslateTransition(CARD_ANIM_DURATION, card),
                new FadeTransition(CARD_ANIM_DURATION, card)
            );
            ((TranslateTransition)pt.getChildren().get(0)).setToX(0);
            ((FadeTransition)pt.getChildren().get(1)).setToValue(1);
            
            pt.setOnFinished(e -> timerequestcontainer.getChildren().add(card));
            pt.play();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeTimeRequestCard(String id) {
        timeRequestList.removeIf(req -> req.id.equals(id));
        
        Node cardToRemove = timerequestcontainer.getChildren().stream()
            .filter(node -> {
                TimerequestcardController controller = (TimerequestcardController) node.getUserData();
                return controller != null && controller.getId().equals(id);
            })
            .findFirst()
            .orElse(null);

        if (cardToRemove != null) {
            // Exit animation
            ParallelTransition exitAnim = new ParallelTransition(
                new TranslateTransition(Duration.millis(250), cardToRemove),
                new FadeTransition(Duration.millis(250), cardToRemove)
            );
            ((TranslateTransition)exitAnim.getChildren().get(0)).setByX(50);
            ((FadeTransition)exitAnim.getChildren().get(1)).setToValue(0);
            
            exitAnim.setOnFinished(e -> timerequestcontainer.getChildren().remove(cardToRemove));
            exitAnim.play();
        }
    }

    public void reloadTimeRequestCards() throws SQLException, IOException {
        // Clear existing cards with animation
        ParallelTransition exitAll = new ParallelTransition();
        timerequestcontainer.getChildren().forEach(node -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setToValue(0);
            exitAll.getChildren().add(ft);
        });
        
        exitAll.setOnFinished(e -> {
            timerequestcontainer.getChildren().clear();
            
            try {
                // Load new cards with staggered animation
                for (int i = 0; i < timeRequestList.size(); i++) {
                    TimeRequest r = timeRequestList.get(i);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/timerequestcard.fxml"));
                    AnchorPane card = loader.load();
                    TimerequestcardController controller = loader.getController();
                    controller.setData(r.id, r.clientName, r.seconds);
                    controller.setParentCard(card);
                    
                    // Staggered entrance
                    card.setOpacity(0);
                    card.setTranslateY(20);
                    
                    PauseTransition delay = new PauseTransition(Duration.millis(i * 50));
                    ParallelTransition enter = new ParallelTransition(
                        new TranslateTransition(Duration.millis(300), card),
                        new FadeTransition(Duration.millis(300), card)
                    );
                    enter.setOnFinished(ev -> {
                        card.setTranslateY(0);
                        card.setOpacity(1);
                    });
                    
                    SequentialTransition seq = new SequentialTransition(delay, enter);
                    timerequestcontainer.getChildren().add(card);
                    seq.play();
                }
            } catch (IOException ex) {
                Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        exitAll.play();
    }
    
    private String formattomin(int totalseconds){
        int minutes = (totalseconds / 60);
        return minutes+" mins";
    }
    
    public void stopAutoRefresh() {
        if (refreshTimer != null) refreshTimer.stop();
    }

    public void clearAllRequests() {
        timeRequestList.clear();
        timerequestcontainer.getChildren().clear();
    }
}
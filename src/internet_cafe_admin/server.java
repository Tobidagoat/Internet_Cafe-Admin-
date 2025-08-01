package internet_cafe_admin;

import controller.DashboardController;
import controller.FoodController;
import controller.InvoicepaneController;
import controller.RoomController;
import database.DbConnection;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import model.foods;

public class server {
    
    // Singleton instance
    private static server instance;
    
    // Server components
    private DbConnection db = new DbConnection();
    private Connection con;
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new HashMap<>();
    public static List<Integer> pendingInvoices = new ArrayList<>();
    private static final List<PendingFoodOrder> pendingFoodOrders = new ArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private final Object shutdownLock = new Object();
    private static FoodController foodController;

    // Private constructor for singleton
    private server() {
        // Initialization if needed
    }
    
    
    
    private static class PendingFoodOrder {
        String clientName;
        ArrayList<foods> foodList;

        PendingFoodOrder(String clientName, ArrayList<foods> foodList) {
            this.clientName = clientName;
            this.foodList = foodList;
        }
    }

    // Singleton access method
    public static synchronized server getInstance() {
        if (instance == null) {
            instance = new server();
        }
        return instance;
    }

    // Start the server
    public void startServer() {
        synchronized (shutdownLock) {
            if (isRunning) {
                System.out.println("Server is already running");
                return;
            }

            try {
                con = db.getConnection();
                isRunning = true;
                serverSocket = new ServerSocket(PORT);
                System.out.println("✅ Server started on port " + PORT);

                // Start accept thread
                new Thread(this::acceptConnections).start();

            } catch (ClassNotFoundException | IOException e) {
                System.out.println("❌ Server startup failed: " + e.getMessage());
                isRunning = false;
                cleanUp();
            }
        }
    }

    // Thread for accepting connections
    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                if (isRunning) { // Double-check after accept
                    new Thread(() -> handleClient(socket)).start();
                } else {
                    socket.close();
                }
            } catch (SocketException e) {
                if (isRunning) {
                    System.out.println("⚠️ Server socket error: " + e.getMessage());
                }
            } catch (IOException e) {
                System.out.println("⚠️ Accept error: " + e.getMessage());
            }
        }
    }

    // Stop the server safely
    public void stopServer() {
        synchronized (shutdownLock) {
            if (!isRunning) return;

            System.out.println("🛑 Initiating server shutdown...");
            isRunning = false;

            // Close server socket in new thread to unblock accept()
            new Thread(() -> {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("⚠️ Error closing server socket: " + e.getMessage());
                }
            }).start();

            // Close all clients
            synchronized (clients) {
                System.out.println("🔌 Closing " + clients.size() + " client connections");
                clients.values().forEach(client -> {
                    try {
                        if (!client.socket.isClosed()) {
                            client.socket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("⚠️ Error closing client socket: " + e.getMessage());
                    }
                });
                clients.clear();
            }

            // Close database
            if (con != null) {
                try {
                    con.close();
                    System.out.println("🔒 Database connection closed");
                } catch (SQLException e) {
                    System.out.println("⚠️ Error closing database: " + e.getMessage());
                }
            }

            System.out.println("✅ Server stopped successfully");
        }
    }

    private void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Invalid client name.");
                socket.close();
                return;
            }

            ClientHandler handler = new ClientHandler(name, socket, in, out);
            synchronized (clients) {
                clients.put(name, handler);
            }
            updatePcStatusInDb(name, true);
            System.out.println("Client connected: " + name);
            handler.listen();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendToClient(String fullCommand) {
        if (!fullCommand.startsWith("TO|")) {
            System.out.println("Invalid format. Use TO|<clientName>|<message>");
            return;
        }

        String[] parts = fullCommand.split("\\|", 3);
        if (parts.length < 3) {
            System.out.println("Incomplete command. Use TO|<clientName>|<message>");
            return;
        }

        String name = parts[1];
        String command = parts[2];

        ClientHandler target = clients.get(name);
        if (target != null) {
            target.sendMessage(command);
            System.out.println("Sent to " + name + ": " + command);
        } else {
            System.out.println("Client '" + name + "' not found.");
        }
    }
    
    public void unlockClient(List<String> pcid, List<Integer> userids, int roomid, String packagename, int duration) {
        for (int i = 0; i < pcid.size(); i++) {
            String pcName = pcid.get(i);
            int userId = userids.get(i);
            String unlockMsg = "UNLOCK|" + pcName + "|" + userId + "|" + roomid + "|" + packagename + "|" + duration;
            sendToClient("TO|" + pcName + "|" + unlockMsg);
            System.out.println(unlockMsg);
        }
    }

    static class ClientHandler {
        private final String name;
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private Connection con1;

        public ClientHandler(String name, Socket socket, BufferedReader in, PrintWriter out) {
            this.name = name;
            this.socket = socket;
            this.in = in;
            this.out = out;
            DbConnection db1 = new DbConnection();
            try {
                con1 = db1.getConnection();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void listen() {
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("Received from " + name + ": " + msg);
                        handleCommand(msg, name);
                    }
                } catch (IOException e) {
                    try {
                        String sql = "UPDATE pcs SET isconnected = ? WHERE pc_id = ?";
                        try (PreparedStatement stmt = con1.prepareStatement(sql)) {
                            stmt.setBoolean(1, false);
                            stmt.setInt(2, Integer.parseInt(name.replaceAll("[^0-9]", "")));
                            stmt.executeUpdate();
                            System.out.println("✅ PC " + name + " connected status updated to " + false);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Client disconnected: " + name);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                    clients.remove(name);
                }
            }).start();
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
        
    private static void handleCommand(String msg, String clientName) {
        if (msg.startsWith("REQUEST_ADD_TIME|")) {
            String[] parts = msg.split("\\|");
            if (parts.length == 2) {
                String seconds = parts[1];
                System.out.println("🕒 " + clientName + " requested +" + seconds + " seconds.");
                
                Platform.runLater(() -> {
                    if (DashboardController.instance != null) {
                        try {
                            int sec = Integer.parseInt(seconds);
                            DashboardController.instance.addTimeRequestCard(clientName, sec);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid seconds value from client " + clientName);
                        } catch (SQLException ex) {
                            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("⚠️ DashboardController.instance is null");
                    }
                });
            }
        } else if (msg.startsWith("FoodOrder|")) {
            String[] parts = msg.split("\\|", 2);
             String data = parts[1];
            ArrayList<foods> cartList = new ArrayList<>();

            String[] items = data.split(";");
            for (String itemData : items) {
                String[] fields = itemData.split(",");
                String name = fields[0];
                double price = Double.parseDouble(fields[1]);
                int quantity = Integer.parseInt(fields[2]);
                cartList.add(new foods(name, quantity, price));
                DashboardController.getInstance().logActivity(clientName+" ordered "+name);
            }
            System.out.println("Order from " + clientName + ": " + (parts.length > 1 ? parts[1] : "Unknown item"));
            Platform.runLater(() -> {
                //DashboardController.getInstance().logActivity("Food Order From "+clientName+" has arrived in Food Order Pane");
                if (foodController != null) {
                    try {
                        foodController.addOrderCard(clientName, cartList);
                    } catch (SQLException ex) {
                        Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    System.out.println("⚠️ FoodController is null — queueing food order");
                    pendingFoodOrders.add(new PendingFoodOrder(clientName, cartList));
                }
            });
        } else if (msg.startsWith("SESSION_END|")) {
            String[] parts = msg.split("\\|");
            int pcId = Integer.parseInt(parts[1]);
            int saleId = Integer.parseInt(parts[2]);

            Platform.runLater(() -> {
                DashboardController.getInstance().logActivity("Session for "+"pc"+pcId+" ended");
                DashboardController.getInstance().logActivity("Invoice for pc"+pcId+" has been added in Invoice pane");
                if (RoomController.instance != null) {
                    RoomController.instance.updateCardToNormal(pcId);
                } else {
                    System.out.println("⚠️ RoomController.instance is null");
                }
                
                if (InvoicepaneController.getinstance() != null) {
                    try {
                        InvoicepaneController.getinstance().addinvoice(saleId);
                    } catch (IOException ex) {
                        Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    System.out.println("⚠️ InvoicepaneController.instance is null — queueing invoice");
                    pendingInvoices.add(saleId);
                    InvoicepaneController.getinstance();
                }
            });
        } else {
            System.out.println("Message from " + clientName + ": " + msg);
        }
    }
    
    public void setFoodController(FoodController controller) {
        this.foodController = controller;
        server.foodController = controller;
        
            Platform.runLater(() -> {
        for (PendingFoodOrder order : pendingFoodOrders) {
            try {
                foodController.addOrderCard(order.clientName, order.foodList);
            } catch (SQLException e) {
                Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        pendingFoodOrders.clear();
    });
    }
    private static boolean showConfirmDialog(String pcName, String seconds) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Time Request");
        alert.setHeaderText("⏳ Time Add Request");
        alert.setContentText("PC " + pcName + " is requesting +" + seconds + " seconds.\nAccept?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public ArrayList<String> getConnectedClients() {
        synchronized (clients) {
            return new ArrayList<>(clients.keySet());
        }
    }

    private void updatePcStatusInDb(String pcName, boolean status) throws SQLException {
        String sql = "UPDATE pcs SET isconnected = ? WHERE pc_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setInt(2, Integer.parseInt(pcName.replaceAll("[^0-9]", "")));
            stmt.executeUpdate();
            System.out.println("✅ PC " + pcName + " connected status updated to " + status);
        }
    }

    private void cleanUp() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("⚠️ Cleanup error: " + e.getMessage());
        }
    }

    public boolean isServerRunning() {
        return isRunning;
    }
}
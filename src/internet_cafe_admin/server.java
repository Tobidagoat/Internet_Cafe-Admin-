package internet_cafe_admin;

import controller.DashboardController;
import controller.RoomController;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class server {

    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new HashMap<>();

    // Start the server and accept incoming clients
    public void startServer() {
        System.out.println("Server starting...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
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

            System.out.println("Client connected: " + name);
            handler.listen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send a message to a client using the TO|<name>|<command> format
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
    
    public void unlockClient(List<String> pcid,List<Integer> userids,int roomid,String packagename,int duration){
        for (int i = 0; i < pcid.size(); i++) {
        String pcName = pcid.get(i);
        int userId = (int) userids.get(i);
        String unlockMsg = "UNLOCK|" + pcName + "|" + userId + "|" + roomid + "|" + packagename + "|" + duration;
        sendToClient("TO|" + pcName + "|" + unlockMsg);
        System.out.println(unlockMsg);
    }
    }

    // When too many clients catch 'em all
    static class ClientHandler {
        private final String name;
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        public ClientHandler(String name, Socket socket, BufferedReader in, PrintWriter out) {
            this.name = name;
            this.socket = socket;
            this.in = in;
            this.out = out;
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
            }   catch (SQLException ex) {
                    Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("⚠️ DashboardController.instance is null");
            }
            });

            }
        }else if (msg.startsWith("ORDER|")) {
            String[] parts = msg.split("\\|", 2);
            System.out.println("Order from " + clientName + ": " + (parts.length > 1 ? parts[1] : "Unknown item"));
            

        }else if (msg.startsWith("SESSION_END|")) {
            String[] parts = msg.split("\\|");
            int pcId = Integer.parseInt(parts[1]);
            int saleId =Integer.parseInt(parts[2]);

             Platform.runLater(() -> {
            if (RoomController.instance != null) {
                RoomController.instance.updateCardToNormal(pcId);
            } else {
                System.out.println("⚠️ RoomController.instance is null");
            }
        });
} else {
            System.out.println("Message from " + clientName + ": " + msg);
        }
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

}

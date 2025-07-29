package controller;

import internet_cafe_admin.server;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class InvoicepaneController implements Initializable {

    @FXML
    private HBox invoicecontainer;
    
    // Change to static to persist between instances
    private static final List<AnchorPane> invoiceCards = new ArrayList<>();
    
    private DefaultController mainController;
    public static InvoicepaneController instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    instance = this;
    reloadInvoices();

    for (int saleId : server.pendingInvoices) {
        try {
            addinvoice(saleId);
        } catch (IOException e) {
            
        }
    }
    server.pendingInvoices.clear();
}
    
    public static InvoicepaneController getinstance() {
        System.out.println("[DEBUG] Getting InvoicepaneController instance");
        return instance;
    }
    
    public void setMainController(DefaultController mainController) {
        System.out.println("[DEBUG] Setting main controller");
        this.mainController = mainController;
    }
    
    public void addinvoice(int saleid) throws IOException {
        System.out.println("[DEBUG] Attempting to add invoice for sale: " + saleid);
        
        for (AnchorPane card : invoiceCards) {
            InvoiceController c = (InvoiceController) card.getProperties().get("controller");
            if (c != null && c.getSaleId() == saleid) {
                System.out.println("[DEBUG] Card already exists for sale: " + saleid);
                return;
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice.fxml"));
        AnchorPane card = loader.load();
        InvoiceController controller = loader.getController();
        
        card.getProperties().put("controller", controller);
        card.getProperties().put("saleid", saleid);
        controller.setdata(saleid);
        controller.setparentcard(card);
        
        invoiceCards.add(card);
        invoicecontainer.getChildren().add(card);
        System.out.println("[DEBUG] Card added. Total cards: " + invoiceCards.size());
        triggernotificationevent();
    }
    
    public boolean removeInvoiceBySaleId(int saleid) {
        System.out.println("[DEBUG] Attempting to remove invoice for sale: " + saleid);
        for (int i = 0; i < invoiceCards.size(); i++) {
            AnchorPane card = invoiceCards.get(i);
            InvoiceController c = (InvoiceController) card.getProperties().get("controller");
            if (c != null && c.getSaleId() == saleid) {
                invoicecontainer.getChildren().remove(card);
                invoiceCards.remove(i);
                System.out.println("[DEBUG] Successfully removed invoice for sale: " + saleid);
                return true;
            }
        }
        System.out.println("[DEBUG] No invoice found for sale: " + saleid);
        return false;
    }
    

    public void reloadInvoices() {
        System.out.println("[DEBUG] Reloading " + invoiceCards.size() + " cards");
        invoicecontainer.getChildren().clear();
        invoicecontainer.getChildren().addAll(invoiceCards);
    }

    public void triggernotificationevent() {
    System.out.println("[DEBUG] Triggering notification event");
    if (mainController != null) {
        Platform.runLater(() -> {
            System.out.println("[DEBUG] Running notification on UI thread");
            mainController.showInvoiceNotification();
        });
    } else {
        System.out.println("[WARNING] Main controller is null - cannot show notification");
    }
    }
    
    public void clearAllInvoices() {
        System.out.println("[DEBUG] Clearing all invoices");
        invoicecontainer.getChildren().clear();
        invoiceCards.clear();
    }
}
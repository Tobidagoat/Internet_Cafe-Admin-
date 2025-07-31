/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author MITUSER-2
 */
public class foods {
    private int id;
    private String name;
    private Double price;
    private String imagePath;
    private String foodType;  
    private int stock;
    private String image;
    private int quantity;

    public foods(int id, String name, String foodType, int stock, Double price, String imagePath) {
        this.id = id;
        this.name = name;
        this.foodType = foodType;
        this.stock = stock;
        this.price = price;
        this.imagePath = imagePath;
    }

    // getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public String getImage() {
    return image;
}

    public void setImage(String image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    
    public foods(String name, int quantity, double price) {
        this.name=name;
        this.quantity=quantity;
        this.price=price;
    }

    public foods(int id, String name, Double price, String imagePath, String foodType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.foodType = foodType;
    }

    public foods(String name, Double price, String imagePath, String foodType) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.foodType = foodType;
    }

    public foods() {
        // Empty constructor for editing/updating
    }

    
    

    // Optional: override toString for ComboBox if you ever use foods in ComboBox
    @Override
    public String toString() {
        return name;
    }
    
}

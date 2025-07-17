/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Linn Hein Htet
 */
public class sale_informaiton {
    private int gaming_income;
    private int food_income;
    private String sale_date;

    public sale_informaiton(int gaming_income, int food_income, String sale_date) {
        this.gaming_income = gaming_income;
        this.food_income = food_income;
        this.sale_date = sale_date;
    }

    public int getGaming_income() {
        return gaming_income;
    }

    public void setGaming_income(int gaming_income) {
        this.gaming_income = gaming_income;
    }

    public int getFood_income() {
        return food_income;
    }

    public void setFood_income(int food_income) {
        this.food_income = food_income;
    }

    public String getSale_date() {
        return sale_date;
    }

    public void setSale_date(String sale_date) {
        this.sale_date = sale_date;
    }
    
}

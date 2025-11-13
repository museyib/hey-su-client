package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class MonthlySaleReport {
    private String invCode;
    private String invName;
    private String brandCode;
    private double saleQty;
    private double mainWhsQty;
    private double t20WhsQty;
    private double t29WhsQty;
    private double mainRzvQty;
    private double t20RzvQty;
    private double t29RzvQty;
    private double priceStd;
    private double priceP01;
    private double priceVp4;
}

package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class StockReport {
    private String invCode;
    private String invName;
    private String brandCode;
    private double whsQty;
    private double rzvQty;
    private double priceStd;
    private double priceP01;
    private double priceVp4;
}

package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class SaleCreditMemoRequestItem {
    private String invCode;
    private double qty;
    private double price;
    private double discountRatio;
    private String taxCode;
    private String uom;
    private double uomFactor;
    private int prevTrxId;
    private int prevTrxTypeId;
    private String prevTrxNo;
}

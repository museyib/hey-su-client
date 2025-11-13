package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class ExchangeInfo {
    private String invCode;
    private String invName;
    private String bpCode;
    private String bpName;
    private Integer exchangeLimit;
    private Integer exchangeQuantity;
    private Integer validDays;
    private Integer daysFromLastSale;
    private String phone;
    private String address;
    private String sbeCode;
    private String sbeName;
}

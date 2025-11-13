package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class InvoiceDetails {
    private String trxNo;
    private String trxDate;
    private String invCode;
    private String invName;
    private String bpCode;
    private String bpName;
    private String sbeCode;
    private String sbeName;
    private Integer quantity;
}

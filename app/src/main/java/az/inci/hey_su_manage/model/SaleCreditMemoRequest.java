package az.inci.hey_su_manage.model;

import java.util.List;

import lombok.Data;

@Data
public class SaleCreditMemoRequest {
    private String terminalTrxNo;
    private String cc;
    private String branch;
    private String trxDate;
    private String bpCode;
    private String sbeCode;
    private String whsCode;
    private String payMethodCode;
    private String notes;
    private String userId;
    private String terminalId;
    private List<SaleCreditMemoRequestItem> requestItems;
}

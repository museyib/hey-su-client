package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class PrevTrxForReturnRequest {
    private String bpCode;
    private String invCode;
    private double qty;
}

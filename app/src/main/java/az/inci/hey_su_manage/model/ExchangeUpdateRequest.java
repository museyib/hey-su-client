package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class ExchangeUpdateRequest {
    private String bpCode;
    private String invCode;
    private int exchangeLimit;
    private int validDays;
}

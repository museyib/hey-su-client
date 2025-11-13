package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class ResponseMessage {
    private int statusCode;
    private String title;
    private String body;
    private int iconId;
}

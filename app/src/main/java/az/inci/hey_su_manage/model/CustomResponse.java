package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class CustomResponse {
    private int statusCode;
    private String systemMessage;
    private String developerMessage;
    private Object data;
}

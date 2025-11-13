package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String userId;
    private String password;
}

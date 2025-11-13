package az.inci.hey_su_manage.security;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String applicationName;
    private String username;
    private String password;
    private String secretKey;
}

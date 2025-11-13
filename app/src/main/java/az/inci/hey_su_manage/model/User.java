package az.inci.hey_su_manage.model;

import lombok.Data;

@Data
public class User {
    private String id;
    private String password;
    private String name;
    private String whsCode;
    private String pickGroup;
    private boolean saleCreditMemoFlag;
}

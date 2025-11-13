package az.inci.hey_su_manage.model;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class Whs {
    private String whsCode;
    private String whsName;

    @NonNull
    @Override
    public String toString() {
        return whsCode + " - " + whsName;
    }
}

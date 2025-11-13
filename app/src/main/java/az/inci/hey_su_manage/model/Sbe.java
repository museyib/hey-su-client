package az.inci.hey_su_manage.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class Sbe {
    private String sbeCode;
    private String sbeName;
    private String branch;
    private String cc;
    private String payMethodCode;

    @Override
    @NonNull
    public String toString() {
        return sbeCode + " - " + sbeName;
    }
}

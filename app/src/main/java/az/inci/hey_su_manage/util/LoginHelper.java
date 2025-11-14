package az.inci.hey_su_manage.util;

import static android.R.drawable.ic_dialog_alert;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.BaseActivity;
import az.inci.hey_su_manage.model.LoginRequest;
import az.inci.hey_su_manage.model.User;

public class LoginHelper {
    private final BaseActivity context;
    private final String userId;
    private final String password;
    private final HttpClient httpClient;
    private final Logger logger;

    public LoginHelper(BaseActivity context) {
        this.context = context;
        logger = new Logger(context);
        httpClient = HttpClient.getInstance(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        userId = preferences.getString("last_login_id", "");
        password = preferences.getString("last_login_password", "");

    }

    public void login(Runnable runnable) {
        context.showProgressDialog(true);
        new Thread(() -> {
            String url = context.url("user", "login");
            LoginRequest request = new LoginRequest();
            request.setUserId(userId);
            request.setPassword(password);
            try {
                User user = httpClient.getSimpleObject(url, "POST", request, User.class);
                context.runOnUiThread(() -> {
                    if (user.isSaleCreditMemoFlag()) {
                        runnable.run();
                    } else
                        context.showMessageDialog(context.getString(R.string.warning),
                                context.getString(R.string.not_allowed),
                                ic_dialog_alert);
                });
            } catch (CustomException e) {
                logger.logError(e.getMessage());
                context.runOnUiThread(() -> context.showMessageDialog(context.getString(R.string.error), e.getMessage(), ic_dialog_alert));
            } finally {
                context.runOnUiThread(() -> context.showProgressDialog(false));
            }
        }).start();
    }
}

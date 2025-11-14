package az.inci.hey_su_manage.util;

import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.BaseActivity;

public class DialogHelper {
    private final BaseActivity context;
    private final AlertDialog.Builder messageDialogBuilder;
    private final AlertDialog progressDialog;
    private final LoginHelper loginHelper;
    private final SharedPreferences preferences;
    private String userId;
    private String password;

    public DialogHelper(BaseActivity context) {
        this.context = context;
        messageDialogBuilder = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.progress_dialog_layout,
                context.findViewById(android.R.id.content), false);
        progressDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        loginHelper = new LoginHelper(context);
        userId = preferences.getString("last_login_id", "");
        password = preferences.getString("last_login_password", "");
    }

    public void showProgressDialog(boolean b) {
        if (b) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    public void showMessageDialog(String title, String message, int iconId) {
        AlertDialog messageDialog = messageDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .create();
        messageDialog.show();
    }

    public void showToastMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void showLoginDialog(Runnable runnable) {
        View view = context.getLayoutInflater().inflate(R.layout.login_page,
                context.findViewById(android.R.id.content), false);

        EditText idEdit = view.findViewById(R.id.id_edit);
        EditText passwordEdit = view.findViewById(R.id.password_edit);

        idEdit.setText(userId);
        idEdit.selectAll();
        passwordEdit.setText(password);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        dialogBuilder.setTitle(R.string.enter)
                .setView(view)
                .setPositiveButton(R.string.enter, (dialog, which) -> {
                    userId = idEdit.getText().toString().toUpperCase();
                    password = passwordEdit.getText().toString();

                    if (userId.isEmpty() || password.isEmpty()) {
                        showToastMessage(context.getString(R.string.username_or_password_not_entered));
                        showLoginDialog(runnable);
                    } else {
                        preferences.edit().putString("last_login_id", userId).apply();
                        preferences.edit().putString("last_login_password", password).apply();
                        loginHelper.login(runnable);
                    }
                });

        AlertDialog loginDialog = dialogBuilder.create();
        Objects.requireNonNull(loginDialog.getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        loginDialog.show();
    }
}

package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;
import static android.R.drawable.ic_dialog_info;
import static android.util.Base64.decode;
import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.util.HttpClient;
import az.inci.hey_su_manage.util.Logger;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.model.LoginRequest;
import az.inci.hey_su_manage.model.User;
import az.inci.hey_su_manage.util.SearchTextListener;

public abstract class BaseActivity extends AppCompatActivity {
    private AlertDialog.Builder messageDialogBuilder;
    private AlertDialog progressDialog;
    protected SearchView searchView;
    protected SharedPreferences preferences;
    protected boolean ismiManager;
    protected String userId;
    protected String password;
    protected Logger logger;
    protected HttpClient httpClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        logger = new Logger(this);
        httpClient = HttpClient.getInstance(this);

        userId = preferences.getString("last_login_id", "");
        password = preferences.getString("last_login_password", "");

        Properties properties = new Properties();
        try {
            properties.load(getAssets().open("app.properties"));
        } catch (IOException ignored) {
        }
        ismiManager = Boolean.parseBoolean(properties.getProperty("app.ismi-manager"));

        buildProgressDialog();
        buildMessageDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reassignParameterValues();
    }

    protected void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showProgressDialog(boolean b) {
        if (b) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    protected void showMessageDialog(String title, String message, int iconId) {
        AlertDialog messageDialog = messageDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .create();
        messageDialog.show();
    }

    void buildProgressDialog() {
        View view = getLayoutInflater().inflate(R.layout.progress_dialog_layout,
                findViewById(android.R.id.content), false);
        if (progressDialog == null) {
            progressDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();
        }
    }

    void buildMessageDialog() {
        messageDialogBuilder = new AlertDialog.Builder(this);
    }

    public String url(String... value) {
        StringBuilder sb = new StringBuilder(serviceURL);
        for (String s : value) {
            sb.append("/").append(s);
        }
        return sb.toString();
    }


    void reassignParameterValues() {
        String serviceIp = preferences.getString("service_ip", "185.129.0.46");
        String servicePort = preferences.getString("service_port", "8026");

        serviceURL = "http://" + serviceIp + ":" + servicePort + "/v2";
    }

    void checkForNewVersion(String fileName) {
        showProgressDialog(true);
        new Thread(() -> {
            String url = serviceURL + "/download?file-name=" + fileName;
            try {
                String bytes = httpClient.getSimpleObject(url, "GET", null, String.class);
                if (bytes != null) {
                    byte[] fileBytes = decode(bytes, Base64.DEFAULT);
                    runOnUiThread(() -> {
                        showProgressDialog(false);
                        updateVersion(fileBytes, fileName);
                    });
                }
            } catch (RuntimeException e) {
                logger.logError(e.getMessage());
                showMessageDialog(getString(R.string.error), e.getMessage(), ic_dialog_alert);
            } catch (CustomException e) {
                logger.logError(e.getMessage());
                showMessageDialog(getString(R.string.error), e.getMessage(), ic_dialog_alert);
            }
        }).start();
    }

    private void updateVersion(byte[] bytes, String fileName) {
        if (bytes != null) {
            File file = new File(getExternalFilesDir("/"), fileName + ".apk");
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(bytes);
            } catch (Exception e) {
                showMessageDialog(getString(R.string.info), e.getMessage(), ic_dialog_alert);
                logger.logError(e.getMessage());
            }

            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
            int version = 0;
            try {
                version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                showMessageDialog(getString(R.string.info), e.getMessage(), ic_dialog_alert);
                logger.logError(e.getMessage());
            }

            if (info != null && file.length() > 0 && info.versionCode > version) {
                Intent installIntent;
                Uri uri;
                installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                uri = FileProvider.getUriForFile(this, "az.inci.hey_su_manage.provider",
                        file);
                installIntent.setData(uri);
                installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(installIntent);
            } else {
                showMessageDialog(getString(R.string.info), getString(R.string.no_new_version), ic_dialog_info);
            }
        } else {
            showMessageDialog(getString(R.string.info), getString(R.string.no_new_version), ic_dialog_alert);
        }
    }

    protected void showLoginDialog(Runnable runnable) {
        View view = getLayoutInflater().inflate(R.layout.login_page,
                findViewById(android.R.id.content), false);

        EditText idEdit = view.findViewById(R.id.id_edit);
        EditText passwordEdit = view.findViewById(R.id.password_edit);

        idEdit.setText(userId);
        idEdit.selectAll();
        passwordEdit.setText(password);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(R.string.enter)
                .setView(view)
                .setPositiveButton(R.string.enter, (dialog, which) -> {
                    userId = idEdit.getText().toString().toUpperCase();
                    password = passwordEdit.getText().toString();

                    if (userId.isEmpty() || password.isEmpty()) {
                        showToastMessage(getString(R.string.username_or_password_not_entered));
                        showLoginDialog(runnable);
                    } else {
                        login(runnable);
                    }
                });

        AlertDialog loginDialog = dialogBuilder.create();
        Objects.requireNonNull(loginDialog.getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        loginDialog.show();
    }

    private void login(Runnable runnable) {
        showProgressDialog(true);
        new Thread(() -> {
            String url = url("user", "login");
            LoginRequest request = new LoginRequest();
            request.setUserId(userId);
            request.setPassword(password);
            try {
                User user = httpClient.getSimpleObject(url, "POST", request, User.class);
                runOnUiThread(() -> {
                    preferences.edit().putString("last_login_id", userId).apply();
                    preferences.edit().putString("last_login_password", password).apply();

                    if (user.isSaleCreditMemoFlag()) {
                        runnable.run();
                    } else
                        showMessageDialog(getString(R.string.warning),
                                getString(R.string.not_allowed),
                                ic_dialog_alert);
                });
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    @SuppressLint("HardwareIds")
    public String getDeviceIdString() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public abstract void refreshData();
    public abstract Filter getFilter();

    protected void setEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void createSearchView(Menu menu, SearchTextListener searchTextListener) {
        MenuItem menuItem =menu.findItem(R.id.search);
        searchView = (SearchView) menuItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(searchTextListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        menu.getItem(0).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });

        menu.getItem(1).setOnMenuItemClickListener(item -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.update_version)
                    .setMessage(R.string.want_to_update)
                    .setPositiveButton(R.string.yes, (dialog1, which) -> {
                        if (ismiManager)
                            checkForNewVersion("HeySuManage_Ismi");
                        else
                            checkForNewVersion("HeySuManage");
                    })
                    .setNegativeButton(R.string.no, null)
                    .create();

            dialog.show();
            return true;
        });

        menu.getItem(3).setOnMenuItemClickListener(item -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.device_id)
                    .setMessage(getDeviceIdString())
                    .create();

            dialog.show();
            return true;
        });

        menu.getItem(4).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, LogViewActivity.class));
            return true;
        });

        SearchTextListener searchTextListener = new SearchTextListener(getFilter());
        createSearchView(menu, searchTextListener);

        return true;
    }
}

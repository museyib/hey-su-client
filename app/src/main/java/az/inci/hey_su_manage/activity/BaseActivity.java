package az.inci.hey_su_manage.activity;

import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Filter;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.Properties;

import az.inci.hey_su_manage.util.DialogHelper;
import az.inci.hey_su_manage.util.HttpClient;
import az.inci.hey_su_manage.util.Logger;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.util.SearchTextListener;
import az.inci.hey_su_manage.util.UpdateHelper;

public abstract class BaseActivity extends AppCompatActivity {
    private DialogHelper dialogHelper;
    private UpdateHelper updateHelper;
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
        updateHelper = new UpdateHelper(this);
        dialogHelper = new DialogHelper(this);

        userId = preferences.getString("last_login_id", "");
        password = preferences.getString("last_login_password", "");

        try {
            Properties properties = new Properties();
            properties.load(getAssets().open("app.properties"));
            ismiManager = Boolean.parseBoolean(properties.getProperty("app.ismi-manager"));
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reassignParameterValues();
    }

    public void showProgressDialog(boolean b) {
        dialogHelper.showProgressDialog(b);
    }

    public void showMessageDialog(String title, String message, int iconId) {
        dialogHelper.showMessageDialog(title, message, iconId);
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

    protected void showLoginDialog(Runnable runnable) {
        dialogHelper.showLoginDialog(runnable);
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
                            updateHelper.checkForNewVersion("HeySuManage_Ismi");
                        else
                            updateHelper.checkForNewVersion("HeySuManage");
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

package az.inci.hey_su_manage.util;

import static android.R.drawable.ic_dialog_alert;
import static android.R.drawable.ic_dialog_info;
import static android.util.Base64.decode;
import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.BaseActivity;

public class UpdateHelper {
    private final BaseActivity context;
    private final Logger logger;
    private final HttpClient httpClient;


    public UpdateHelper(BaseActivity context) {
        this.context = context;
        this.logger = new Logger(context);
        this.httpClient = HttpClient.getInstance(context);
    }

    public void checkForNewVersion(String fileName) {
        context.showProgressDialog(true);
        new Thread(() -> {
            String url = serviceURL + "/download?file-name=" + fileName;
            try {
                String bytes = httpClient.getSimpleObject(url, "GET", null, String.class);
                if (bytes != null) {
                    byte[] fileBytes = decode(bytes, Base64.DEFAULT);
                    context.runOnUiThread(() -> {
                        context.showProgressDialog(false);
                        updateVersion(fileBytes, fileName);
                    });
                }
            } catch (RuntimeException e) {
                logger.logError(e.getMessage());
                context.showMessageDialog(context.getString(R.string.error), e.getMessage(), ic_dialog_alert);
            } catch (CustomException e) {
                logger.logError(e.getMessage());
                context.showMessageDialog(context.getString(R.string.error), e.getMessage(), ic_dialog_alert);
            }
        }).start();
    }

    private void updateVersion(byte[] bytes, String fileName) {
        if (bytes != null) {
            File file = new File(context.getExternalFilesDir("/"), fileName + ".apk");
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(bytes);
            } catch (Exception e) {
                context.showMessageDialog(context.getString(R.string.info), e.getMessage(), ic_dialog_alert);
                logger.logError(e.getMessage());
            }

            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
            int version = 0;
            try {
                version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                context.showMessageDialog(context.getString(R.string.info), e.getMessage(), ic_dialog_alert);
                logger.logError(e.getMessage());
            }

            if (info != null && file.length() > 0 && info.versionCode > version) {
                Intent installIntent;
                Uri uri;
                installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                uri = FileProvider.getUriForFile(context, "az.inci.hey_su_manage.provider",
                        file);
                installIntent.setData(uri);
                installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(installIntent);
            } else {
                context.showMessageDialog(context.getString(R.string.info),
                        context.getString(R.string.no_new_version), ic_dialog_info);
            }
        } else {
            context.showMessageDialog(context.getString(R.string.info),
                    context.getString(R.string.no_new_version), ic_dialog_alert);
        }
    }
}

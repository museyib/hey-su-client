package az.inci.hey_su_manage.activity;

import android.os.Bundle;
import android.widget.Filter;

import az.inci.hey_su_manage.R;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setEdgeToEdge();
    }

    @Override
    public void refreshData() {

    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
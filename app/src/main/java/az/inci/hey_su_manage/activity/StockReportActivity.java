package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;
import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.adapter.StockReportAdapter;
import az.inci.hey_su_manage.model.StockReport;
import az.inci.hey_su_manage.model.Whs;

public class StockReportActivity extends BaseActivity {
    public List<Whs> whsList = new ArrayList<>();
    public List<StockReport> reportDataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Spinner whsSpinner;

    private String whsCode;
    private String lastSearch = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_report);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!searchView.isIconified())
                    searchView.setIconified(true);
                else
                    finish();
            }
        });

        recyclerView = findViewById(R.id.data_list);
        whsSpinner = findViewById(R.id.whs_list);
        whsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Whs whs = (Whs) whsSpinner.getItemAtPosition(position);
                whsCode = whs.getWhsCode();
                refreshData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> refreshData());

        loadWhsList();
    }

    @Override
    public void refreshData() {
        if (!TextUtils.isEmpty(whsCode)) {
            showProgressDialog(true);
            new Thread(() -> {
                String url = serviceURL + "/report/stock?whs-code=" + whsCode;
                try {
                    reportDataList = httpClient.getListData(url, "GET", null, StockReport[].class);
                    runOnUiThread(() -> {
                        StockReportAdapter adapter = new StockReportAdapter(reportDataList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(adapter);
                        filter(lastSearch);
                    });
                } catch (CustomException e) {
                    logger.logError(e.toString());
                    runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
                } finally {
                    runOnUiThread(() -> showProgressDialog(false));
                }
            }).start();
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<StockReport> filteredData = new ArrayList<>();
                constraint = constraint.toString().toLowerCase();
                for (StockReport reportItem : reportDataList) {
                    if (reportItem.getInvCode().concat(reportItem.getInvName()).concat(
                            reportItem.getBrandCode()).toLowerCase().contains(constraint))
                        filteredData.add(reportItem);
                }

                results.count = filteredData.size();
                results.values = filteredData;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                List<StockReport> localDataList = (List<StockReport>) results.values;
                StockReportAdapter adapter = (StockReportAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setLocalDataList(localDataList);
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    private void loadWhsList() {
        showProgressDialog(true);
        new Thread(() -> {
            String url = serviceURL + "/whs";
            try {
                whsList = httpClient.getListData(url, "GET", null, Whs[].class);
                runOnUiThread(() -> {
                    ArrayAdapter<Whs> whsArrayAdapter =
                            new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, whsList);
                    whsSpinner.setAdapter(whsArrayAdapter);
                    whsCode = whsList.get(0).getWhsCode();
                });
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    private void filter(String text) {
        lastSearch = text;
        getFilter().filter(text);
    }
}
package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;
import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.adapter.SaleReportAdapter;
import az.inci.hey_su_manage.model.MonthlySaleReport;

public class SaleReportActivity extends BaseActivity {
    public List<MonthlySaleReport> reportDataList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private RecyclerView recyclerView;
    private LocalDate startDate;
    private LocalDate endDate;
    private EditText startDateEdit;
    private EditText endDateEdit;
    private String lastSearch = "";
    private int orderDirection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_report);

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
        startDateEdit = findViewById(R.id.start_date);
        endDateEdit = findViewById(R.id.end_date);

        startDate = LocalDate.now().minusMonths(1);
        endDate = LocalDate.now();
        startDateEdit.setText(startDate.format(formatter));
        endDateEdit.setText(endDate.format(formatter));

        Button refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(v -> refreshData());

        startDateEdit.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(this);
            dialog.getDatePicker().updateDate(startDate.getYear(),
                    startDate.getMonthValue() - 1,
                    startDate.getDayOfMonth());
            dialog.setOnDateSetListener((datePicker, year, month, day) -> {
                startDate = LocalDate.of(year, month + 1, day);
                startDateEdit.setText(startDate.format(formatter));
            });
            dialog.show();
        });

        endDateEdit.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(this);
            dialog.getDatePicker().updateDate(endDate.getYear(),
                    endDate.getMonthValue() - 1,
                    endDate.getDayOfMonth());
            dialog.setOnDateSetListener((datePicker, year, month, day) -> {
                endDate = LocalDate.of(year, month + 1, day);
                endDateEdit.setText(endDate.format(formatter));
            });
            dialog.show();
        });
    }

    @Override
    public void refreshData() {
        try {
            startDate = LocalDate.parse(startDateEdit.getText());
            endDate = LocalDate.parse(endDateEdit.getText());
        } catch (DateTimeParseException e) {
            showMessageDialog(getString(R.string.error), "Tarix formatı düzgün deyil!", ic_dialog_alert);
        }
        showProgressDialog(true);
        new Thread(() -> {
            String url = String.format(
                    "%s/report/sale-from-date-interval?start-date=%s&end-date=%s",
                    serviceURL, startDate.format(formatter), endDate.format(formatter));
            try {
                reportDataList = httpClient.getListData(url, "GET", null, MonthlySaleReport[].class);
                runOnUiThread(() -> {
                    SaleReportAdapter adapter = new SaleReportAdapter(reportDataList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    filter(lastSearch);

                    findViewById(R.id.sale_label).setOnClickListener(v -> order(adapter, "sale"));
                    findViewById(R.id.main_whs_label).setOnClickListener(v -> order(adapter, "mainWhs"));
                    findViewById(R.id.main_rzv_label).setOnClickListener(v -> order(adapter, "mainRzv"));
                    findViewById(R.id.t20_whs_label).setOnClickListener(v -> order(adapter, "t20Whs"));
                    findViewById(R.id.t20_rzv_label).setOnClickListener(v -> order(adapter, "t20Rzv"));
                    findViewById(R.id.t29_whs_label).setOnClickListener(v -> order(adapter, "t29Whs"));
                    findViewById(R.id.t29_rzv_label).setOnClickListener(v -> order(adapter, "t29Rzv"));
                });
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<MonthlySaleReport> filteredData = new ArrayList<>();
                constraint = constraint.toString().toLowerCase();
                for (MonthlySaleReport reportItem : reportDataList) {
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
                List<MonthlySaleReport> localDataList = (List<MonthlySaleReport>) results.values;
                SaleReportAdapter adapter = (SaleReportAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setLocalDataList(localDataList);
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    private void filter(String text) {
        lastSearch = text;
        getFilter().filter(text);

    }

    private void order(RecyclerView.Adapter<SaleReportAdapter.ViewHolder> adapter, String orderBy) {
        List<MonthlySaleReport> reportDataList = ((SaleReportAdapter) adapter).getLocalDataList();
        orderDirection *= -1;
        switch (orderBy) {
            case "sale":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getSaleQty() * orderDirection)));
                break;
            case "mainWhs":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getMainWhsQty() * orderDirection)));
                break;
            case "mainRzv":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getMainRzvQty() * orderDirection)));
                break;
            case "t20Whs":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getT20WhsQty() * orderDirection)));
                break;
            case "t20Rzv":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getT20RzvQty() * orderDirection)));
                break;
            case "t29Whs":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getT29WhsQty() * orderDirection)));
                break;
            case "t29Rzv":
                reportDataList.sort(Comparator.comparingDouble(o -> (o.getT29RzvQty() * orderDirection)));
                break;
        }
        adapter.notifyItemRangeChanged(0, reportDataList.size());
    }
}
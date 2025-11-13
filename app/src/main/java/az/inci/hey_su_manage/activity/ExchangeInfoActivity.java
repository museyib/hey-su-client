package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.adapter.ExchangeInfoAdapter;
import az.inci.hey_su_manage.model.ExchangeInfo;

public class ExchangeInfoActivity extends BaseActivity {
    public List<ExchangeInfo> exchangeInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private int orderDirection = 1;
    private String orderBy = "";
    private String invCode;
    private String lastSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_info);

        invCode = getIntent().getStringExtra("invCode");

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> refreshData());
        refreshData();
    }

    @Override
    public void refreshData() {
        loadExchangeInfo(invCode);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<ExchangeInfo> filteredData = new ArrayList<>();
                constraint = constraint.toString().toLowerCase();
                for (ExchangeInfo reportItem : exchangeInfoList) {
                    if (reportItem.getBpCode().concat(reportItem.getBpName())
                            .toLowerCase().contains(constraint))
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
                List<ExchangeInfo> localDataList = (List<ExchangeInfo>) results.values;
                ExchangeInfoAdapter adapter = (ExchangeInfoAdapter) recyclerView.getAdapter();
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

    public void loadExchangeInfo(String invCode) {
        showProgressDialog(true);
        recyclerView.setAdapter(null);
        new Thread(() -> {
            String url = url("exchange", "info") + "?inv-code=" + invCode;
            try {
                exchangeInfoList = httpClient.getListData(url, "GET", null, ExchangeInfo[].class);
                runOnUiThread(() -> {
                    ExchangeInfoAdapter adapter = new ExchangeInfoAdapter(exchangeInfoList, this);
                    recyclerView.setAdapter(adapter);
                    filter(lastSearch);
                    findViewById(R.id.limit).setOnClickListener(v -> order("limit", adapter));
                    findViewById(R.id.qty_at_client).setOnClickListener(v -> order("qty", adapter));
                    findViewById(R.id.limit_remained).setOnClickListener(v -> order("limitRemained", adapter));
                    findViewById(R.id.valid_days).setOnClickListener(v -> order("validDays", adapter));
                    findViewById(R.id.valid_days_remained).setOnClickListener(v -> order("validDaysRemained", adapter));
                });
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    private void order(String orderBy, RecyclerView.Adapter<ExchangeInfoAdapter.ViewHolder> adapter) {
        List<ExchangeInfo> exchangeInfoList = ((ExchangeInfoAdapter) adapter).getLocalDataList();
        if (this.orderBy.equals(orderBy)) {
            orderDirection *= -1;
        } else
            this.orderBy = orderBy;

        switch (this.orderBy) {
            case "qty":
                exchangeInfoList.sort(Comparator.comparingInt(o -> o.getExchangeQuantity() * orderDirection));
                break;
            case "limit":
                exchangeInfoList.sort(Comparator.comparingInt(o -> o.getExchangeLimit() * orderDirection));
                break;
            case "limitRemained":
                exchangeInfoList.sort(Comparator.comparingInt(o -> (o.getExchangeLimit() - o.getExchangeQuantity()) * orderDirection));
                break;
            case "validDays":
                exchangeInfoList.sort(Comparator.comparingInt(o -> o.getValidDays() * orderDirection));
                break;
            case "validDaysRemained":
                exchangeInfoList.sort(Comparator.comparingInt(o -> (o.getValidDays() - o.getDaysFromLastSale()) * orderDirection));
                break;
        }
        adapter.notifyItemRangeChanged(0, exchangeInfoList.size());
    }

    public void getPrevTrxDataForReturn(Map<String, String> dataMapForReturn) {
        View view = getLayoutInflater().inflate(R.layout.enter_qty_dialog,
                findViewById(android.R.id.content), false);

        EditText qtyEdit = view.findViewById(R.id.qty_edit);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(R.string.qty_to_return)
                .setView(view)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String bpCode = null;
                    String bpName = null;
                    String invCode = null;
                    String invName = null;
                    Set<Map.Entry<String, String>> entries = dataMapForReturn.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        switch (entry.getKey()) {
                            case "bpCode":
                                bpCode = entry.getValue();
                                break;
                            case "bpName":
                                bpName = entry.getValue();
                                break;
                            case "invCode":
                                invCode = entry.getValue();
                                break;
                            case "invName":
                                invName = entry.getValue();
                                break;
                        }
                    }
                    int qty = Integer.parseInt(qtyEdit.getText().toString());
                    Intent intent = new Intent(this, SaleCreditMemoActivity.class);
                    intent.putExtra("bpCode", bpCode);
                    intent.putExtra("bpName", bpName);
                    intent.putExtra("invCode", invCode);
                    intent.putExtra("invName", invName);
                    intent.putExtra("qty", qty);
                    startActivity(intent);
                });

        AlertDialog loginDialog = dialogBuilder.create();
        Objects.requireNonNull(loginDialog.getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        loginDialog.show();
    }
}
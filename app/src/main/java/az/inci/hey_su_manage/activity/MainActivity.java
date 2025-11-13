package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Filter;
import android.widget.RadioButton;
import android.widget.TableRow;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.adapter.CustomAdapter;
import az.inci.hey_su_manage.adapter.ExchangeGroupedInfoAdapter;
import az.inci.hey_su_manage.adapter.InvoiceDetailsAdapter;
import az.inci.hey_su_manage.adapter.OrderDetailsAdapter;
import az.inci.hey_su_manage.model.ExchangeInfo;
import az.inci.hey_su_manage.model.InvoiceDetails;
import az.inci.hey_su_manage.model.OrderDetails;

public class MainActivity extends BaseActivity {

    public List<OrderDetails> orderDetailsList = new ArrayList<>();
    public List<ExchangeInfo> exchangeInfoList = new ArrayList<>();
    public List<InvoiceDetails> invoiceDetailsList = new ArrayList<>();


    RadioButton ordersButton;
    RadioButton exchangeInfoButton;
    RadioButton invoicesButton;
    private RecyclerView recyclerView;
    private TableRow ordersHeader;
    private TableRow deliverInvoiceHeader;
    private TableRow exchangeInfoHeader;
    private TableRow exchangeGroupedInfoHeader;
    private String lastSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        ordersButton = findViewById(R.id.orders);
        exchangeInfoButton = findViewById(R.id.bp_inv_sum);
        invoicesButton = findViewById(R.id.deliver_invoice);

        ordersHeader = findViewById(R.id.orders_data_header);
        deliverInvoiceHeader = findViewById(R.id.deliver_invoice_data_header);
        exchangeInfoHeader = findViewById(R.id.exchange_info_data_header);
        exchangeGroupedInfoHeader = findViewById(R.id.exchange_grouped_info_data_header);

        ordersButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                loadOrders();
        });

        exchangeInfoButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                loadExchangeGroupedInfo();
        });

        invoicesButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                loadInvoices();
        });

        Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> refreshData());

        Button saleReport = findViewById(R.id.sale_report);
        saleReport.setOnClickListener(v -> startActivity(new Intent(this, SaleReportActivity.class)));

        Button stockReport = findViewById(R.id.stock_report);
        stockReport.setOnClickListener(v -> startActivity(new Intent(this, StockReportActivity.class)));

        if (ismiManager) {
            saleReport.setVisibility(VISIBLE);
            stockReport.setVisibility(VISIBLE);
        } else {
            saleReport.setVisibility(GONE);
            stockReport.setVisibility(GONE);
        }
    }

    private void loadOrders() {
        showProgressDialog(true);
        recyclerView.setAdapter(null);
        new Thread(() -> {
            try {
                orderDetailsList = httpClient.getListData(url("order", "get-details"),
                        "GET", null, OrderDetails[].class);
                runOnUiThread(() -> {
                    ordersHeader.setVisibility(VISIBLE);
                    exchangeGroupedInfoHeader.setVisibility(GONE);
                    exchangeInfoHeader.setVisibility(GONE);
                    deliverInvoiceHeader.setVisibility(GONE);
                    OrderDetailsAdapter adapter = new OrderDetailsAdapter(orderDetailsList, this);
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

    private void loadExchangeGroupedInfo() {
        showProgressDialog(true);
        recyclerView.setAdapter(null);
        new Thread(() -> {
            try {
                exchangeInfoList = httpClient.getListData(url("exchange", "info-grouped"),
                        "GET", null, ExchangeInfo[].class);
                runOnUiThread(() -> {
                    ordersHeader.setVisibility(GONE);
                    exchangeGroupedInfoHeader.setVisibility(VISIBLE);
                    exchangeInfoHeader.setVisibility(GONE);
                    deliverInvoiceHeader.setVisibility(GONE);
                    ExchangeGroupedInfoAdapter adapter = new ExchangeGroupedInfoAdapter(exchangeInfoList, this);
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

    private void loadInvoices() {
        showProgressDialog(true);
        recyclerView.setAdapter(null);
        new Thread(() -> {
            try {
                invoiceDetailsList = httpClient.getListData(url("invoice", "get-details"),
                        "GET", null, InvoiceDetails[].class);
                invoiceDetailsList.addAll(httpClient.getListData(url("deliver", "get-details"),
                        "GET", null, InvoiceDetails[].class));
                runOnUiThread(() -> {
                    ordersHeader.setVisibility(GONE);
                    exchangeGroupedInfoHeader.setVisibility(GONE);
                    exchangeInfoHeader.setVisibility(GONE);
                    deliverInvoiceHeader.setVisibility(VISIBLE);
                    InvoiceDetailsAdapter adapter = new InvoiceDetailsAdapter(invoiceDetailsList);
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

    public void refreshData() {
        if (ordersButton.isChecked())
            loadOrders();
        else if (exchangeInfoButton.isChecked())
            loadExchangeGroupedInfo();
        else if (invoicesButton.isChecked())
            loadInvoices();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Object> filteredData = new ArrayList<>();
                if (ordersButton.isChecked()) {
                    constraint = constraint.toString().toLowerCase();
                    for (OrderDetails reportItem : orderDetailsList) {
                        if (reportItem.getBpCode().concat(reportItem.getBpName()).toLowerCase().contains(constraint))
                            filteredData.add(reportItem);
                    }
                }
                else if (exchangeInfoButton.isChecked()) {
                    constraint = constraint.toString().toLowerCase();
                    for (ExchangeInfo reportItem : exchangeInfoList) {
                        if (reportItem.getInvCode().concat(reportItem.getInvName())
                                .toLowerCase().contains(constraint))
                            filteredData.add(reportItem);
                    }
                }
                else if (invoicesButton.isChecked()) {
                    constraint = constraint.toString().toLowerCase();
                    for (InvoiceDetails reportItem : invoiceDetailsList) {
                        if (reportItem.getBpCode().concat(reportItem.getBpName())
                                .toLowerCase().contains(constraint))
                            filteredData.add(reportItem);
                    }
                }

                results.count = filteredData.size();
                results.values = filteredData;
                return results;
            }

            @SuppressWarnings("unchecked")
            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<?> localDataList;
                if (ordersButton.isChecked())
                    localDataList = (List<OrderDetails>) results.values;
                else if (exchangeInfoButton.isChecked())
                    localDataList = (List<ExchangeInfo>) results.values;
                else if (invoicesButton.isChecked())
                    localDataList = (List<InvoiceDetails>) results.values;
                else
                    return;

                CustomAdapter<RecyclerView.ViewHolder> adapter = (CustomAdapter<RecyclerView.ViewHolder>) recyclerView.getAdapter();
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
}
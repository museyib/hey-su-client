package az.inci.hey_su_manage.activity;

import static android.R.drawable.ic_dialog_alert;
import static android.text.TextUtils.isEmpty;
import static androidx.appcompat.R.layout.support_simple_spinner_dropdown_item;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.adapter.SaleCreditMemoAdapter;
import az.inci.hey_su_manage.model.PrevTrxForReturnRequest;
import az.inci.hey_su_manage.model.PrevTrxForReturnResponse;
import az.inci.hey_su_manage.model.ResponseMessage;
import az.inci.hey_su_manage.model.SaleCreditMemoRequest;
import az.inci.hey_su_manage.model.SaleCreditMemoRequestItem;
import az.inci.hey_su_manage.model.Sbe;
import az.inci.hey_su_manage.model.Whs;

public class SaleCreditMemoActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Spinner whsListSpinner;
    private Spinner sbeListSpinner;
    private List<PrevTrxForReturnResponse> responseData;
    private List<Whs> whsList;
    private List<Sbe> sbeList;
    private String branch;
    private String cc;
    private String payMethodCode;
    private String bpCode;
    private String sbeCode;
    private String whsCode;
    private String invCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_credit_memo);
        recyclerView = findViewById(R.id.trx_list_view);
        whsListSpinner = findViewById(R.id.whs_list);
        sbeListSpinner = findViewById(R.id.sbe_list);

        whsListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                whsCode = ((Whs) parent.getItemAtPosition(position)).getWhsCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sbeListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sbeCode = ((Sbe) parent.getItemAtPosition(position)).getSbeCode();
                branch = ((Sbe) parent.getItemAtPosition(position)).getBranch();
                cc = ((Sbe) parent.getItemAtPosition(position)).getCc();
                payMethodCode = ((Sbe) parent.getItemAtPosition(position)).getPayMethodCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Intent intent = getIntent();
        bpCode = intent.getStringExtra("bpCode");
        invCode = intent.getStringExtra("invCode");
        String bpName = intent.getStringExtra("bpName");
        String invName = intent.getStringExtra("invName");
        double qty = intent.getIntExtra("qty", 1);

        TextView infoText = findViewById(R.id.info);
        infoText.setText(String.format("%s - %s", bpCode, bpName));
        infoText.append(String.format("\n%s - %s", invCode, invName));

        getPrevTrxData(bpCode, invCode, qty);

        ImageButton send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
            if (!isEmpty(whsCode) && !isEmpty(sbeCode))

                createSaleCreditMemo();
        });
    }

    @Override
    public void refreshData() {

    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void getPrevTrxData(String bpCode, String invCode, double qty) {
        showProgressDialog(true);
        new Thread(() -> {
            PrevTrxForReturnRequest request = new PrevTrxForReturnRequest();
            request.setBpCode(bpCode);
            request.setInvCode(invCode);
            request.setQty(qty);
            try {
                responseData = httpClient.getListData(url("sale-credit-memo", "prev-trx"),
                        "POST", request, PrevTrxForReturnResponse[].class);
                runOnUiThread(() -> {
                    if (responseData.isEmpty())
                        showMessageDialog(getString(R.string.info),
                                "Bu müştəri və mal üzrə qaimə tapılmadı!",
                                ic_dialog_alert);
                    else {
                        double qtySum = getQtySum();
                        double returnableQty = qty;

                        if (qtySum < qty) {
                            returnableQty = qtySum;
                        } else if (qtySum > qty) {
                            PrevTrxForReturnResponse responseItem = responseData.get(responseData.size() - 1);
                            responseItem.setQty(responseItem.getQty() - (qtySum - qty));
                        }
                        ((TextView) findViewById(R.id.returnable_qty)).setText(String.format("Qaytarıla bilən say: %s", returnableQty));

                        showLoginDialog(() -> {
                            SaleCreditMemoAdapter adapter = new SaleCreditMemoAdapter(responseData, this);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            recyclerView.setAdapter(adapter);
                            getWhsList();
                            getSbeList(bpCode, invCode);
                        });
                    }
                });
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    public void getWhsList() {
        showProgressDialog(true);
        new Thread(() -> {
            String url = url("whs", "for-user");
            url = url.concat("?user-id=").concat(userId);
            try {
                whsList = httpClient.getListData(url, "GET", null, Whs[].class);
                runOnUiThread(() -> whsListSpinner.setAdapter(new ArrayAdapter<>(this, support_simple_spinner_dropdown_item, whsList)));
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    public void getSbeList(String bpCode, String invCode) {
        showProgressDialog(true);
        new Thread(() -> {
            String url = url("sbe", "list");
            url = url.concat("?bp-code=").concat(bpCode)
                    .concat("&inv-code=").concat(invCode);
            try {
                sbeList = httpClient.getListData(url, "GET", null, Sbe[].class);
                runOnUiThread(() -> sbeListSpinner.setAdapter(new ArrayAdapter<>(this, support_simple_spinner_dropdown_item, sbeList)));
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    private double getQtySum() {
        double qtySum = 0;

        for (PrevTrxForReturnResponse item : responseData) {
            qtySum += item.getQty();
        }

        return qtySum;
    }

    private void createSaleCreditMemo() {
        showProgressDialog(true);
        new Thread(() -> {
            SaleCreditMemoRequest request = getSaleCreditMemoRequest();

            for (PrevTrxForReturnResponse item : responseData) {
                SaleCreditMemoRequestItem requestItem = getSaleCreditMemoRequestItem(item);
                request.getRequestItems().add(requestItem);
            }
            try {
                ResponseMessage message = httpClient.executeUpdate(url("sale-credit-memo", "create"), request);
                if (message.getStatusCode() == 0)
                    finish();
            } catch (CustomException e) {
                logger.logError(e.toString());
                runOnUiThread(() -> showMessageDialog(getString(R.string.error), e.toString(), ic_dialog_alert));
            } finally {
                runOnUiThread(() -> showProgressDialog(false));
            }
        }).start();
    }

    @NonNull
    private SaleCreditMemoRequest getSaleCreditMemoRequest() {
        LocalDateTime dateTime = LocalDateTime.now();
        String trxDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String trxNo = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        SaleCreditMemoRequest request = new SaleCreditMemoRequest();
        request.setCc(cc);
        request.setBranch(branch);
        request.setTrxDate(trxDate);
        request.setTerminalTrxNo(trxNo);
        request.setBpCode(bpCode);
        request.setSbeCode(sbeCode);
        request.setWhsCode(whsCode);
        request.setPayMethodCode(payMethodCode);
        request.setUserId(userId);
        request.setTerminalId(getDeviceIdString());
        request.setRequestItems(new ArrayList<>());
        return request;
    }

    @NonNull
    private SaleCreditMemoRequestItem getSaleCreditMemoRequestItem(PrevTrxForReturnResponse item) {
        SaleCreditMemoRequestItem requestItem = new SaleCreditMemoRequestItem();
        requestItem.setInvCode(invCode);
        requestItem.setQty(item.getQty());
        requestItem.setPrice(item.getPrice());
        requestItem.setDiscountRatio(item.getDiscountRatio());
        requestItem.setTaxCode(item.getTaxCode());
        requestItem.setUom(item.getUom());
        requestItem.setUomFactor(item.getUomFactor());
        requestItem.setPrevTrxId(item.getTrxId());
        requestItem.setPrevTrxTypeId(item.getTrxTypeId());
        requestItem.setPrevTrxNo(item.getTrxNo());
        return requestItem;
    }
}
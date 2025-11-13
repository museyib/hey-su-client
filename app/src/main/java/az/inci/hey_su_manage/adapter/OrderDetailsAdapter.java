package az.inci.hey_su_manage.adapter;


import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.BaseActivity;
import az.inci.hey_su_manage.model.ExchangeUpdateRequest;
import az.inci.hey_su_manage.model.OrderDetails;
import az.inci.hey_su_manage.util.HttpClient;
import az.inci.hey_su_manage.util.Logger;

public class OrderDetailsAdapter extends CustomAdapter<OrderDetailsAdapter.ViewHolder> {
    private final Context context;
    private List<OrderDetails> localDataList;
    private final HttpClient httpClient;
    private final Logger logger;

    public OrderDetailsAdapter(List<OrderDetails> dataList, Context context) {
        localDataList = dataList;
        this.context = context;
        httpClient = HttpClient.getInstance(context);
        logger = new Logger(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View orderDetailsView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.order_details_item, parent, false);

        return new ViewHolder(orderDetailsView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetails orderDetails = localDataList.get(position);

        int limitRemained =
                orderDetails.getExchangeLimit() - orderDetails.getExchangeQuantity();
        int validDaysRemained =
                orderDetails.getValidDays() - orderDetails.getDaysFromLastSale();
        holder.trxNo.setText(orderDetails.getTrxNo());
        holder.trxDate.setText(orderDetails.getTrxDate());
        holder.invCode.setText(orderDetails.getInvCode());
        holder.invName.setText(orderDetails.getInvName());
        holder.bpCode.setText(orderDetails.getBpCode());
        holder.bpName.setText(orderDetails.getBpName());
        holder.sbeCode.setText(orderDetails.getSbeCode());
        holder.sbeName.setText(orderDetails.getSbeName());
        holder.quantity.setText(String.valueOf(orderDetails.getQuantity()));
        holder.limit.setText(String.valueOf(orderDetails.getExchangeLimit()));
        holder.qtyAtClient.setText(String.valueOf(orderDetails.getExchangeQuantity()));
        holder.limitRemained.setText(String.valueOf(limitRemained));
        holder.validDays.setText(String.valueOf(orderDetails.getValidDays()));
        holder.validDaysRemained.setText(String.valueOf(validDaysRemained));
        holder.recStatus.setText(orderDetails.getRecStatus());
        holder.pickStatus.setText(orderDetails.getPickStatus());

        if (orderDetails.getExchangeLimit() == 0) {
            holder.trxNo.getRootView().setBackgroundColor(Color.YELLOW);
        } else if (limitRemained <= 0 || validDaysRemained <= 0) {
            holder.trxNo.getRootView().setBackgroundColor(0xFFF87979);
        } else {
            holder.trxNo.getRootView().setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            String invCode = orderDetails.getInvCode();
            String invName = orderDetails.getInvName();
            String bpCode = orderDetails.getBpCode();
            String bpName = orderDetails.getBpName();
            int limit = orderDetails.getExchangeLimit();
            int validDays = orderDetails.getValidDays();
            int orderQuantity = orderDetails.getQuantity();

            View alertDialogView = ((BaseActivity) context).getLayoutInflater().inflate(R.layout.edit_exchange_limits, null);

            TextView bpInfoText = alertDialogView.findViewById(R.id.bp_info);
            TextView invInfoText = alertDialogView.findViewById(R.id.inv_info);
            TextView limitText = alertDialogView.findViewById(R.id.exchange_limit);
            TextView validDaysText = alertDialogView.findViewById(R.id.valid_days);

            bpInfoText.setText(String.format("%s - %s", bpCode, bpName));
            invInfoText.setText(String.format("%s - %s", invCode, invName));

            AlertDialog alertDialog;
            if (limit > 0) {
                limitText.setText(String.valueOf(limit));
                validDaysText.setText(String.valueOf(validDays));
                alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Mübadilə limitini dəyiş")
                        .setView(alertDialogView)
                        .setPositiveButton("Qeyd et", (dialog, which) -> {
                            ExchangeUpdateRequest request = new ExchangeUpdateRequest();
                            request.setInvCode(invCode);
                            request.setBpCode(bpCode);
                            request.setExchangeLimit(Integer.parseInt(limitText.getText().toString()));
                            request.setValidDays(Integer.parseInt(validDaysText.getText().toString()));
                            sendExchangeUpdateRequest(request, position);
                        })
                        .setNegativeButton("İmtina", null)
                        .create();
            } else {
                limitText.setText(String.valueOf(orderQuantity * 2));
                validDaysText.setText(String.valueOf(10));
                alertDialog = new AlertDialog.Builder(context)
                        .setMessage(
                                "DİQQƏT!! Bu müştəri üçün mübadilə limiti təyin edilməyib. Cari sifarişə uyğun limit təyin etmək istəyirsinizmi?")
                        .setView(alertDialogView)
                        .setPositiveButton("Bəli", (dialog, which) -> {
                            ExchangeUpdateRequest request = new ExchangeUpdateRequest();
                            request.setInvCode(invCode);
                            request.setBpCode(bpCode);
                            request.setExchangeLimit(orderQuantity * 2);
                            request.setValidDays(10);
                            sendExchangeUpdateRequest(request, position);
                        })
                        .setNegativeButton("Xeyr", null)
                        .create();
            }
            alertDialog.show();
        });
    }

    private void sendExchangeUpdateRequest(ExchangeUpdateRequest request, int position) {
        new Thread(() -> {
            try {
                httpClient.executeUpdate(serviceURL + "/exchange/update", request);
                ((Activity) context).runOnUiThread(() -> notifyItemChanged(position));
            } catch (CustomException e) {
                logger.logError(e.getMessage());
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    @Override
    public <T> void setLocalDataList(List<T> localDataList) {
        //noinspection unchecked
        this.localDataList = (List<OrderDetails>) localDataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView trxNo;
        private final TextView trxDate;
        private final TextView invCode;
        private final TextView invName;
        private final TextView bpCode;
        private final TextView bpName;
        private final TextView sbeCode;
        private final TextView sbeName;
        private final TextView quantity;
        private final TextView limit;
        private final TextView qtyAtClient;
        private final TextView limitRemained;
        private final TextView validDays;
        private final TextView validDaysRemained;
        private final TextView recStatus;
        private final TextView pickStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            trxNo = itemView.findViewById(R.id.trx_no);
            trxDate = itemView.findViewById(R.id.trx_date);
            invCode = itemView.findViewById(R.id.inv_code);
            invName = itemView.findViewById(R.id.inv_name);
            bpCode = itemView.findViewById(R.id.bp_code);
            bpName = itemView.findViewById(R.id.bp_name);
            sbeCode = itemView.findViewById(R.id.sbe_code);
            sbeName = itemView.findViewById(R.id.sbe_name);
            quantity = itemView.findViewById(R.id.quantity);
            limit = itemView.findViewById(R.id.limit);
            qtyAtClient = itemView.findViewById(R.id.qty_at_client);
            limitRemained = itemView.findViewById(R.id.limit_remained);
            validDays = itemView.findViewById(R.id.valid_days);
            validDaysRemained = itemView.findViewById(R.id.valid_days_remained);
            recStatus = itemView.findViewById(R.id.rec_status);
            pickStatus = itemView.findViewById(R.id.pick_status);
        }
    }
}
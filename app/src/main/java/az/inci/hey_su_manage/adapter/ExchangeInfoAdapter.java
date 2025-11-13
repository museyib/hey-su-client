package az.inci.hey_su_manage.adapter;


import static az.inci.hey_su_manage.util.GlobalParameters.serviceURL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import az.inci.hey_su_manage.CustomException;
import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.BaseActivity;
import az.inci.hey_su_manage.activity.ExchangeInfoActivity;
import az.inci.hey_su_manage.model.ExchangeInfo;
import az.inci.hey_su_manage.model.ExchangeUpdateRequest;
import az.inci.hey_su_manage.util.HttpClient;
import az.inci.hey_su_manage.util.Logger;
import lombok.Getter;
import lombok.Setter;

public class ExchangeInfoAdapter extends RecyclerView.Adapter<ExchangeInfoAdapter.ViewHolder> {
    private final Context context;
    @Getter
    @Setter
    private List<ExchangeInfo> localDataList;
    private final HttpClient httpClient;
    private final Logger logger;

    public ExchangeInfoAdapter(List<ExchangeInfo> dataList, Context context) {
        localDataList = dataList;
        this.context = context;
        httpClient = HttpClient.getInstance(context);
        logger = new Logger(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.exchange_info_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExchangeInfo exchangeInfo = localDataList.get(position);

        holder.itemView.setOnClickListener(v -> {
            String invCode = exchangeInfo.getInvCode();
            String invName = exchangeInfo.getInvName();
            String bpCode = exchangeInfo.getBpCode();
            String bpName = exchangeInfo.getBpName();
            int limit = exchangeInfo.getExchangeLimit();
            int validDays = exchangeInfo.getValidDays();

            View alertDialogView = LayoutInflater
                    .from(context)
                    .inflate(R.layout.edit_exchange_limits, null, false);

            TextView bpInfoText = alertDialogView.findViewById(R.id.bp_info);
            TextView invInfoText = alertDialogView.findViewById(R.id.inv_info);
            TextView limitText = alertDialogView.findViewById(R.id.exchange_limit);
            TextView validDaysText = alertDialogView.findViewById(R.id.valid_days);

            bpInfoText.setText(String.format("%s - %s", bpCode, bpName));
            invInfoText.setText(String.format("%s - %s", invCode, invName));

            AlertDialog alertDialog;
            limitText.setText(String.valueOf(limit));
            validDaysText.setText(String.valueOf(validDays));
            alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Mübadilə limitini dəyiş")
                    .setView(alertDialogView)
                    .setPositiveButton("Qeyd et", (dialog, which) -> {
                        ExchangeUpdateRequest request = new ExchangeUpdateRequest();
                        request.setInvCode(invCode);
                        request.setBpCode(bpCode);
                        request.setExchangeLimit(
                                Integer.parseInt(limitText.getText().toString()));
                        request.setValidDays(
                                Integer.parseInt(validDaysText.getText().toString()));
                        new Thread(() -> {
                            try {
                                httpClient.executeUpdate(serviceURL + "/exchange/update", request);
                                ((Activity) context).runOnUiThread(() -> notifyItemChanged(position));
                            } catch (CustomException e) {
                                logger.logError(e.toString());
                            }
                        }).start();
                    })
                    .setNegativeButton("İmtina", null)
                    .create();
            alertDialog.show();
        });

        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            MenuInflater menuInflater = ((BaseActivity) context).getMenuInflater();
            menuInflater.inflate(R.menu.context_menu, menu);

            menu.getItem(0).setOnMenuItemClickListener(item -> {
                String invCode = exchangeInfo.getInvCode();
                String invName = exchangeInfo.getInvName();
                String bpCode = exchangeInfo.getBpCode();
                String bpName = exchangeInfo.getBpName();
                Map<String, String> dataMapForReturn = new HashMap<>();
                dataMapForReturn.put("bpCode", bpCode);
                dataMapForReturn.put("bpName", bpName);
                dataMapForReturn.put("invCode", invCode);
                dataMapForReturn.put("invName", invName);
                ((ExchangeInfoActivity) context).getPrevTrxDataForReturn(dataMapForReturn);
                return true;
            });
        });

        int limitRemained =
                exchangeInfo.getExchangeLimit() - exchangeInfo.getExchangeQuantity();
        int validDaysRemained =
                exchangeInfo.getValidDays() - exchangeInfo.getDaysFromLastSale();
        holder.invCode.setText(exchangeInfo.getInvCode());
        holder.invName.setText(exchangeInfo.getInvName());
        holder.bpCode.setText(exchangeInfo.getBpCode());
        holder.bpName.setText(exchangeInfo.getBpName());
        holder.sbeName.setText(String.format("%s - %s", exchangeInfo.getSbeCode(), exchangeInfo.getSbeName()));
        holder.limit.setText(String.valueOf(exchangeInfo.getExchangeLimit()));
        holder.qtyAtClient.setText(String.valueOf(exchangeInfo.getExchangeQuantity()));
        holder.limitRemained.setText(String.valueOf(limitRemained));
        holder.validDays.setText(String.valueOf(exchangeInfo.getValidDays()));
        holder.validDaysRemained.setText(String.valueOf(validDaysRemained));
        holder.phone.setText(exchangeInfo.getPhone());
        holder.address.setText(exchangeInfo.getAddress());

        if (limitRemained <= 0 || validDaysRemained <= 0) {
            holder.invCode.getRootView().setBackgroundColor(0xFFF87979);
        } else {
            holder.invCode.getRootView().setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView invCode;
        private final TextView invName;
        private final TextView bpCode;
        private final TextView bpName;
        private final TextView sbeName;
        private final TextView limit;
        private final TextView qtyAtClient;
        private final TextView limitRemained;
        private final TextView validDays;
        private final TextView validDaysRemained;
        private final TextView phone;
        private final TextView address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            invCode = itemView.findViewById(R.id.inv_code);
            invName = itemView.findViewById(R.id.inv_name);
            bpCode = itemView.findViewById(R.id.bp_code);
            bpName = itemView.findViewById(R.id.bp_name);
            sbeName = itemView.findViewById(R.id.sbe_name);
            limit = itemView.findViewById(R.id.limit);
            qtyAtClient = itemView.findViewById(R.id.qty_at_client);
            limitRemained = itemView.findViewById(R.id.limit_remained);
            validDays = itemView.findViewById(R.id.valid_days);
            validDaysRemained = itemView.findViewById(R.id.valid_days_remained);
            phone = itemView.findViewById(R.id.phone);
            address = itemView.findViewById(R.id.address);
        }
    }
}
package az.inci.hey_su_manage.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.activity.ExchangeInfoActivity;
import az.inci.hey_su_manage.model.ExchangeInfo;

public class ExchangeGroupedInfoAdapter extends CustomAdapter<ExchangeGroupedInfoAdapter.ViewHolder> {
    private final Context context;
    private List<ExchangeInfo> localDataList;

    public ExchangeGroupedInfoAdapter(List<ExchangeInfo> dataList, Context context) {
        localDataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.exchange_grouped_info_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExchangeInfo exchangeInfo = localDataList.get(position);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExchangeInfoActivity.class);
            intent.putExtra("invCode", exchangeInfo.getInvCode());
            context.startActivity(intent);
        });
        holder.invCode.setText(exchangeInfo.getInvCode());
        holder.invName.setText(exchangeInfo.getInvName());
        holder.qtyAtClient.setText(String.valueOf(exchangeInfo.getExchangeQuantity()));
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    @Override
    public <T> void setLocalDataList(List<T> localDataList) {
        //noinspection unchecked
        this.localDataList = (List<ExchangeInfo>) localDataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView invCode;
        private final TextView invName;
        private final TextView qtyAtClient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            invCode = itemView.findViewById(R.id.inv_code);
            invName = itemView.findViewById(R.id.inv_name);
            qtyAtClient = itemView.findViewById(R.id.qty_at_client);
        }
    }
}
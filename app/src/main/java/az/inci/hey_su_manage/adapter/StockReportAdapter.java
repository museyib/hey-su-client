package az.inci.hey_su_manage.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.model.StockReport;
import lombok.Setter;

public class StockReportAdapter extends RecyclerView.Adapter<StockReportAdapter.ViewHolder> {
    @Setter
    private List<StockReport> localDataList;

    public StockReportAdapter(List<StockReport> dataList) {
        localDataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View reportItemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.stock_report_item, parent, false);
        return new ViewHolder(reportItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockReport stockReport = localDataList.get(position);

        holder.invCode.setText(stockReport.getInvCode());
        holder.invName.setText(stockReport.getInvName());
        holder.brandCode.setText(stockReport.getBrandCode());
        holder.whsQty.setText(String.valueOf(stockReport.getWhsQty()));
        holder.rzvQty.setText(String.valueOf(stockReport.getRzvQty()));
        holder.priceStd.setText(String.valueOf(stockReport.getPriceStd()));
        holder.priceP01.setText(String.valueOf(stockReport.getPriceP01()));
        holder.priceVp4.setText(String.valueOf(stockReport.getPriceVp4()));
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView invCode;
        private final TextView invName;
        private final TextView brandCode;
        private final TextView whsQty;
        private final TextView rzvQty;
        private final TextView priceStd;
        private final TextView priceP01;
        private final TextView priceVp4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            invCode = itemView.findViewById(R.id.inv_code);
            invName = itemView.findViewById(R.id.inv_name);
            brandCode = itemView.findViewById(R.id.brand_code);
            whsQty = itemView.findViewById(R.id.whs_qty);
            rzvQty = itemView.findViewById(R.id.rzv_qty);
            priceStd = itemView.findViewById(R.id.price_std);
            priceP01 = itemView.findViewById(R.id.price_p01);
            priceVp4 = itemView.findViewById(R.id.price_vp4);
        }
    }
}
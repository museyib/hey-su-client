package az.inci.hey_su_manage.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.model.MonthlySaleReport;
import lombok.Getter;
import lombok.Setter;

public class SaleReportAdapter extends RecyclerView.Adapter<SaleReportAdapter.ViewHolder> {
    @Getter
    @Setter
    private List<MonthlySaleReport> localDataList;

    public SaleReportAdapter(List<MonthlySaleReport> dataList) {
        localDataList = dataList;
    }

    @NonNull
    @Override
    public SaleReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View reportItemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.sale_report_item, parent, false);
        return new ViewHolder(reportItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleReportAdapter.ViewHolder holder, int position) {
        MonthlySaleReport saleReport = localDataList.get(position);

        holder.invCode.setText(saleReport.getInvCode());
        holder.invName.setText(saleReport.getInvName());
        holder.brandCode.setText(saleReport.getBrandCode());
        holder.sale.setText(String.valueOf(saleReport.getSaleQty()));
        holder.mainWhsQty.setText(String.valueOf(saleReport.getMainWhsQty()));
        holder.t20WhsQty.setText(String.valueOf(saleReport.getT20WhsQty()));
        holder.t29WhsQty.setText(String.valueOf(saleReport.getT29WhsQty()));
        holder.mainRzvQty.setText(String.valueOf(saleReport.getMainRzvQty()));
        holder.t20RzvQty.setText(String.valueOf(saleReport.getT20RzvQty()));
        holder.t29RzvQty.setText(String.valueOf(saleReport.getT29RzvQty()));
        holder.priceStd.setText(String.valueOf(saleReport.getPriceStd()));
        holder.priceP01.setText(String.valueOf(saleReport.getPriceP01()));
        holder.priceVp4.setText(String.valueOf(saleReport.getPriceVp4()));
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView invCode;
        private final TextView invName;
        private final TextView brandCode;
        private final TextView sale;
        private final TextView mainWhsQty;
        private final TextView t20WhsQty;
        private final TextView t29WhsQty;
        private final TextView mainRzvQty;
        private final TextView t20RzvQty;
        private final TextView t29RzvQty;
        private final TextView priceStd;
        private final TextView priceP01;
        private final TextView priceVp4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            invCode = itemView.findViewById(R.id.inv_code);
            invName = itemView.findViewById(R.id.inv_name);
            brandCode = itemView.findViewById(R.id.brand_code);
            sale = itemView.findViewById(R.id.sale);
            mainWhsQty = itemView.findViewById(R.id.main_whs_qty);
            t20WhsQty = itemView.findViewById(R.id.t20_whs_qty);
            t29WhsQty = itemView.findViewById(R.id.t29_whs_qty);
            mainRzvQty = itemView.findViewById(R.id.main_rzv_qty);
            t20RzvQty = itemView.findViewById(R.id.t20_rzv_qty);
            t29RzvQty = itemView.findViewById(R.id.t29_rzv_qty);
            priceStd = itemView.findViewById(R.id.price_std);
            priceP01 = itemView.findViewById(R.id.price_p01);
            priceVp4 = itemView.findViewById(R.id.price_vp4);
        }
    }
}
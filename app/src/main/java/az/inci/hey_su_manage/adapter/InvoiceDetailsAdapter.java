package az.inci.hey_su_manage.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.model.InvoiceDetails;

public class InvoiceDetailsAdapter extends CustomAdapter<InvoiceDetailsAdapter.ViewHolder> {
    private List<InvoiceDetails> localDataList;

    public InvoiceDetailsAdapter(List<InvoiceDetails> dataList) {
        localDataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View detailsView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.invoice_details_item, parent, false);

        return new ViewHolder(detailsView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceDetails details = localDataList.get(position);
        holder.trxNo.setText(details.getTrxNo());
        holder.trxDate.setText(details.getTrxDate());
        holder.invCode.setText(details.getInvCode());
        holder.invName.setText(details.getInvName());
        holder.bpCode.setText(details.getBpCode());
        holder.bpName.setText(details.getBpName());
        holder.sbeCode.setText(details.getSbeCode());
        holder.sbeName.setText(details.getSbeName());
        holder.quantity.setText(String.valueOf(details.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    @Override
    public <T> void setLocalDataList(List<T> localDataList) {
        //noinspection unchecked
        this.localDataList = (List<InvoiceDetails>) localDataList;
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
        }
    }
}
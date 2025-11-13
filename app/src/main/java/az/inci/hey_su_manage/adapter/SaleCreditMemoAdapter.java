package az.inci.hey_su_manage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import az.inci.hey_su_manage.R;
import az.inci.hey_su_manage.model.PrevTrxForReturnResponse;

/**
 * @noinspection ClassEscapesDefinedScope
 */
public class SaleCreditMemoAdapter extends RecyclerView.Adapter<SaleCreditMemoAdapter.ViewHolder> {
    private final Context context;
    private final List<PrevTrxForReturnResponse> localDataList;

    public SaleCreditMemoAdapter(List<PrevTrxForReturnResponse> localDataList, Context context) {
        this.localDataList = localDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(context)
                .inflate(R.layout.sale_credit_memo_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrevTrxForReturnResponse response = localDataList.get(position);
        holder.trxNo.setText(response.getTrxNo());
        holder.trxDate.setText(response.getTrxDate());
        holder.qty.setText(String.valueOf(response.getQty()));
    }

    @Override
    public int getItemCount() {
        return localDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView trxNo;
        private final TextView trxDate;
        private final TextView qty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            trxNo = itemView.findViewById(R.id.trx_no);
            trxDate = itemView.findViewById(R.id.trx_date);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}

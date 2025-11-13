package az.inci.hey_su_manage.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class CustomAdapter<H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {
    public abstract <T> void setLocalDataList (List<T> localDataList);
}

package az.inci.hey_su_manage.util;


import android.widget.Filter;
import android.widget.SearchView;

public class SearchTextListener implements SearchView.OnQueryTextListener {
    private final Filter filter;

    public SearchTextListener(Filter filter) {
        this.filter = filter;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter.filter(newText);
        return true;
    }
}
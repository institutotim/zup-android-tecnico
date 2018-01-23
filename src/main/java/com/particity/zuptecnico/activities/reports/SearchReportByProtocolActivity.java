package com.particity.zuptecnico.activities.reports;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.ReportsAdapter;
import com.particity.zuptecnico.ui.UIHelper;
import com.particity.zuptecnico.util.QueryChecker;

public class SearchReportByProtocolActivity extends ReportsListActivity implements SearchView.OnCloseListener, MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener, QueryChecker.UpdatableQuery {
    private String query;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_reports_list, menu);

        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint(getString(R.string.search_by_protocol_hint));
        search.setOnCloseListener(this);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), this);
        }
        search.setOnQueryTextListener(this);
        search.setIconifiedByDefault(false);
        search.requestFocus();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(FilterReportsActivity.FILTER_OPTIONS)) {
            optionsQuery = getIntent().getParcelableExtra(FilterReportsActivity.FILTER_OPTIONS);
            String order = getIntent().getStringExtra(ReportsAdapter.ORDER);
            String sort = getIntent().getStringExtra(ReportsAdapter.SORT);
            adapter.setOrder(order);
            adapter.setSort(sort);
            if (optionsQuery != null) {
                adapter.setFilterOptions(optionsQuery.getQueryMap());
                adapter.reset();
            }
        }
    }

    @Override
    void loadPage() {
        updateQuery();
    }

    void updateQuery(String query) {
        setQuery(query);
        updateQuery();
    }

    void setQuery(String query) {
        this.query = query;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQuery();
        new QueryChecker(this).start();
    }

	@Override
    public void updateQuery() {
        showLoading();
        adapter.setQuery(query);
    }
	
	@Override
	public String getQuery() {
		return query;
	}

    @Override
    protected void loadUI() {
        UIHelper.initActivity(this);
    }

    @Override
    public boolean onClose() {
        updateQuery("");
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        updateQuery("");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        setQuery(query);
        return false;
    }
}

package com.particity.zuptecnico.activities.inventory;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ui.UIHelper;
import com.particity.zuptecnico.util.QueryChecker;

/**
 * Created by Renan on 06/01/2016.
 */
public class SearchInventoryByQueryActivity extends InventoryListActivity implements SearchView.OnCloseListener, MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener, QueryChecker.UpdatableQuery {
    private String query;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_reports_list, menu);

        menu.findItem(R.id.action_order_address).setVisible(false);
        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint(getString(R.string.search_by_name_address_hint));
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
    public void updateQuery(){
        showLoading();
        mAdapter.setQuery(query);
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

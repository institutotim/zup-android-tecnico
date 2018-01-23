package com.particity.zuptecnico.activities.cases;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ui.UIHelper;

public class SearchCasesActivity extends CasesListActivity {
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIHelper.initActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_reports_list, menu);

        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint(getString(R.string.search_cases_hint));
        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                finish();
                return true;
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                updateQuery(query);
                return true;
            }
        });
        search.setIconifiedByDefault(false);
        search.requestFocus();
        return true;
    }

    @Override
    void loadPage() {
        updateQuery();
    }

    void updateQuery(String query) {
        this.query = query;
        updateQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQuery();
    }

    void updateQuery() {
        if (query != null && !query.trim().isEmpty()) {
            adapter.setQuery(query);
            showLoading();
        } else {
            adapter.clear();
            hideLoading();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
}

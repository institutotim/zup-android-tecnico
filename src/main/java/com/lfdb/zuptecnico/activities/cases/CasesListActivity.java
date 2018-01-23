package com.lfdb.zuptecnico.activities.cases;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.activities.RootActivity;
import com.lfdb.zuptecnico.adapters.CasesAdapter;
import com.lfdb.zuptecnico.adapters.OfflineCasesAdapter;
import com.lfdb.zuptecnico.ui.UIHelper;

public class CasesListActivity extends RootActivity implements AdapterView.OnItemClickListener,
        CasesAdapter.CasesAdapterListener {

    ListView mListView;
    CasesAdapter adapter;
    boolean isOffline;
    android.support.v7.widget.PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_cases_list);
        loadUI();

        refreshPopupMenu();
        selectFlow(-1, getString(R.string.all_cases_title));


        adapter = new CasesAdapter(this);
        adapter.setListener(this);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(adapter);
        mListView.setDividerHeight(0);
        mListView.setOnItemClickListener(this);

        findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOnline();
            }
        });
    }

    private void loadUI() {
        popupMenu = UIHelper.initMenu(this);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectFlow(menuItem);
                return true;
            }
        });
    }

    private void selectFlow(MenuItem menuItem) {
        selectFlow(menuItem.getItemId(), menuItem.getTitle().toString());
    }

    private void selectFlow(int id, String title) {
        if (adapter != null) {
            adapter.clear();
            adapter.setFlowId(id);
        }
        UIHelper.setTitle(this, title);
    }

    private void refreshPopupMenu() {
        popupMenu.getMenu().clear();
        popupMenu.getMenu().add(Menu.NONE, -1, 0, R.string.all_cases_title);
    }

    void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.filter_by_status_title));

        String[] items = new String[4];
        items[0] = getString(R.string.all_status_filter);
        items[1] = getString(R.string.pending_filter_title);
        items[2] = getString(R.string.in_execution);
        items[3] = getString(R.string.done_filter_title);


        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.setFilter(indexToStatus(i));
                dialogInterface.dismiss();
            }

            private String indexToStatus(int id) {
                switch (id) {
                    case 1:
                        return "pending";
                    case 2:
                        return "active";
                    case 3:
                        return "finished";
                    case 0:
                    default:
                        return "";
                }
            }
        });
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cases_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_items_filter) {
            showFilterDialog();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            loadPage();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, SearchCasesActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_order_modification) {
            adapter.orderBy(CasesAdapter.ORDER_BY_LAST_EDITION);
        } else if (item.getItemId() == R.id.action_order_creation) {
            adapter.orderBy(CasesAdapter.ORDER_BY_CREATION);
        } else if (item.getItemId() == R.id.action_order_name) {
            adapter.orderBy(CasesAdapter.ORDER_BY_NAME);
        }
        return super.onOptionsItemSelected(item);
    }

    public void openCaseItem(int id) {
        Intent intent = new Intent(this, CaseItemDetailsActivity.class);
        intent.putExtra(CaseItemDetailsActivity.CASE_ID, id);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPage();
    }

    void showNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
    }

    void hideNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long itemId) {
        openCaseItem(adapter.getItem(i).id);
    }

    void showLoading() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    void loadPage() {
        showLoading();
        adapter.reset();
    }

    @Override
    public void onCasesLoaded() {
        this.hideLoading();
    }

    @Override
    public void onEmptyResultsLoaded() {
        ZupApplication.toast(findViewById(android.R.id.content), getString(R.string.no_results_found)).show();
        hideLoading();
    }

    private void showOfflineList() {
        adapter = new OfflineCasesAdapter(this);
        adapter.setListener(this);
        mListView.setAdapter(adapter);
    }

    @Override
    public void onNetworkError() {
        showOfflineList();


        this.isOffline = true;
        showNoConnectionBar();
        hideLoading();
        refreshPopupMenu();
    }

    void goOnline() {
        adapter = new CasesAdapter(this);
        adapter.setListener(this);
        mListView.setAdapter(adapter);

        this.isOffline = false;
        hideNoConnectionBar();
        loadPage();
    }

    @Override
    public void updateDrawerStatus() {
        TextView labelDocuments = (TextView) findViewById(R.id.sidebar_label_documents);
        ImageView iconDocuments = (ImageView)findViewById(R.id.sidebar_icon_documents);

        labelDocuments.setTextColor(ContextCompat.getColor(this, R.color.zupblue));
        iconDocuments.setColorFilter(ContextCompat.getColor(this, R.color.zupblue), PorterDuff.Mode.SRC_ATOP);
    }
}

package com.particity.zuptecnico.activities.reports;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.SyncActivity;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.activities.RootActivity;
import com.particity.zuptecnico.adapters.OfflineReportsAdapter;
import com.particity.zuptecnico.adapters.ReportsAdapter;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.sync.DeleteReportItemSyncAction;
import com.particity.zuptecnico.api.sync.EditReportItemSyncAction;
import com.particity.zuptecnico.api.sync.PublishReportItemSyncAction;
import com.particity.zuptecnico.entities.ReportItem;
import com.particity.zuptecnico.fragments.reports.FilterReportsFragment;
import com.particity.zuptecnico.fragments.reports.ReportCategorySelectorDialog;
import com.particity.zuptecnico.fragments.reports.ReportsMapFragment;
import com.particity.zuptecnico.ui.UIHelper;
import com.particity.zuptecnico.util.ItemsAdapterListener;




public class ReportsListActivity extends RootActivity implements AdapterView.OnItemClickListener,
        ItemsAdapterListener, ReportsMapFragment.Listener {
    private static final int REQUEST_SHOWREPORT = 1;
    private static final int REQUEST_FILTER = 2;

    ListView mListView;
    ReportsAdapter adapter;
    boolean isOffline;
    ReportsMapFragment mMapFragment;
    private View mMapContainer;
    FilterReportsFragment.FilterOptions optionsQuery;

    private Menu mMenu;
    private boolean mMapMode;
    private Snackbar toast;
    private BroadcastReceiver editedReceiver;
    private BroadcastReceiver deletedReceiver;
    private BroadcastReceiver createdReceiver;

    private void getPermissions() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 231);
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    return;
  }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_reports_list);

        loadUI();
        toast = ZupApplication.toast(findViewById(android.R.id.content),R.string.no_results_found);

        this.adapter = new ReportsAdapter(this);
        this.adapter.setListener(this);

        mMapContainer = findViewById(R.id.reports_map_container);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(this.adapter);
        mListView.setOnScrollListener(this.adapter);
        mListView.setDividerHeight(0);
        mListView.setOnItemClickListener(this);

        findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOnline();
            }
        });

        boolean canCreate = Zup.getInstance().getAccess().canCreateReportItem();
        findViewById(R.id.report_create_button).setVisibility(canCreate ? View.VISIBLE : View.GONE);

        turnsGpsOn();
        getPermissions();
    }

    public void turnsGpsOn() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
        manager.unregisterReceiver(createdReceiver);
        manager.unregisterReceiver(deletedReceiver);
        manager.unregisterReceiver(editedReceiver);

    }

    protected void loadUI() {
        UIHelper.setTitle(this, getString(R.string.activity_title_reports));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_order_creation:
                adapter.setOrder(ReportsAdapter.Order.CreationDate);
                return true;

            case R.id.action_order_address:
                adapter.setOrder(ReportsAdapter.Order.Address);
                return true;

            case R.id.action_items_filter:
                intent = new Intent(this, FilterReportsActivity.class);
                if (optionsQuery != null) {
                    intent.putExtra(FilterReportsActivity.FILTER_OPTIONS, optionsQuery);
                }
                this.startActivityForResult(intent, REQUEST_FILTER);
                return true;
            case R.id.action_search:
                intent = new Intent(this, SearchReportByProtocolActivity.class);
                if (optionsQuery != null) {
                    intent.putExtra(FilterReportsActivity.FILTER_OPTIONS, optionsQuery);
                }
                intent.putExtra(ReportsAdapter.ORDER, adapter.getOrder());
                intent.putExtra(ReportsAdapter.SORT, adapter.getSort());
                startActivity(intent);
                return true;
            case R.id.action_map:
                showMap();
                return true;
            case R.id.action_list:
                hideMap();
                return true;
            case R.id.action_items_clear_filter:
                if(adapter != null) {
                    adapter.setFilterOptions(null);
                    optionsQuery = null;
                    loadPage();
                }
                item.setVisible(false);
                return true;
            case R.id.action_refresh:
                if (adapter != null) {
                    adapter.reset();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        getMenuInflater().inflate(R.menu.reports_list, menu);
        refreshMenu();

        return true;
    }

    @Override
    public void openReportItem(int id) {
        if (Zup.getInstance().getSyncActionService().hasSyncActionRelatedToReportItem(id)) {
            showPendingSyncReportDialog();
            return;
        }
        Intent intent = new Intent(this, ReportItemDetailsActivity.class);
        intent.putExtra("item_id", (int) id);
        startActivityForResult(intent, REQUEST_SHOWREPORT);
    }

    private void showPendingSyncReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.pending_sync_error_message));
        builder.setPositiveButton(getString(R.string.sync_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ReportsListActivity.this, SyncActivity.class);
                intent.putExtra("sync_now", true);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void refreshMenu() {
        if(mMenu == null) {
            return;
        }
        mMenu.findItem(R.id.action_list).setVisible(mMapMode);
        mMenu.findItem(R.id.action_map).setVisible(!mMapMode);
        mMenu.findItem(R.id.action_items_clear_filter).setVisible(adapter != null && adapter.isFiltered());
    }

    private void showMap() {
        if (mMapFragment == null) {
            mMapFragment = new ReportsMapFragment();
            mMapFragment.setListener(this);
        }

        mMapContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.reports_map_container, mMapFragment)
                .commit();

        mMapMode = true;
        refreshMenu();
    }

    private void hideMap() {
        if (mMapFragment == null) {
            return;
        }

        mMapContainer.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction()
                .remove(mMapFragment)
                .commit();

        mMapMode = false;
        refreshMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(createdReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportPublished((ReportItem) intent.getParcelableExtra("report"));
            }
        }, new IntentFilter(PublishReportItemSyncAction.REPORT_PUBLISHED));
        manager.registerReceiver(deletedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportDeleted(intent.getIntExtra("report_id", -1));
            }
        }, new IntentFilter(DeleteReportItemSyncAction.REPORT_DELETED));
        manager.registerReceiver(editedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportEdited((ReportItem) intent.getParcelableExtra("report"));
            }
        }, new IntentFilter(EditReportItemSyncAction.REPORT_EDITED));

        // Update our listing because we may be offline now
        loadPage();
    }

    private void reportEdited(ReportItem item) {
        // Load list again
        loadPage();
    }

    void reportDeleted(int id) {
        // Load list again
        loadPage();

        ZupApplication.toast(findViewById(android.R.id.content),R.string.report_deleted).show();
    }

    void showNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
    }

    void hideNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.GONE);
    }

    void reportPublished(ReportItem item) {
        if (item == null)
            return;

        ZupApplication.toast(findViewById(android.R.id.content),R.string.message_report_created).show();
        openReportItem(item.id);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long itemId) {
        if (itemId <= 0){

        }
        openReportItem((int) itemId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            optionsQuery = data.getParcelableExtra(FilterReportsActivity.FILTER_OPTIONS);
            if (optionsQuery != null) {
                this.adapter.setFilterOptions(optionsQuery.getQueryMap());
            }
            showLoading();
        } else if (requestCode == REQUEST_SHOWREPORT
                && resultCode == ReportItemDetailsActivity.RESULT_DELETED) {
            // Remove all items and show the loading bar while the report is being deleted
            this.adapter.reset();
            showLoading();

            ZupApplication.toast(findViewById(android.R.id.content),R.string.deleting_report).show();
        } else if (requestCode == REQUEST_SHOWREPORT
                && resultCode == ReportItemDetailsActivity.RESULT_CHANGED) {
            this.adapter.reset();

            if (!isOffline) {
                showLoading();
            }
        }
        refreshMenu();
    }

    public void showCreateDialog(View sender) {
        ReportCategorySelectorDialog dialog = new ReportCategorySelectorDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit", false);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "category_list");
        dialog.setListener(new ReportCategorySelectorDialog.OnReportCategorySetListener() {
            @Override
            public void onReportCategorySet(int categoryId) {
                createItem(categoryId);
            }
        });
    }

    void createItem(int categoryId) {
        Intent intent = new Intent(this, CreateReportItemActivity.class);
        intent.putExtra("categoryId", categoryId);
        startActivityForResult(intent, REQUEST_SHOWREPORT);
    }

    void showLoading() {
        hideResultsView();
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    void loadPage() {
        showLoading();
        this.adapter.reset();
    }

    @Override
    public void onItemsLoaded() {
        this.hideLoading();
        if (adapter.getSizeOfRealList() > 0) {
            showResultsView();
        } else {
            hideResultsView();
        }
    }

    @Override
    public void onEmptyResultsLoaded() {
        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast.show();
        }
        hideLoading();

    }

    private void showResultsView() {
        findViewById(R.id.layout_result_search).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.results_size_text)).setText(String.valueOf(adapter.getSizeOfRealList()));
    }

    private void hideResultsView() {
        findViewById(R.id.layout_result_search).setVisibility(View.GONE);
    }

    @Override
    public void onNetworkError() {
        this.adapter = new OfflineReportsAdapter(this);
        this.adapter.setListener(this);
        mListView.setAdapter(this.adapter);

        this.isOffline = true;
        showNoConnectionBar();
        hideLoading();

    }

    void goOnline() {
        this.adapter = new ReportsAdapter(this);
        this.adapter.setListener(this);
        mListView.setAdapter(this.adapter);
        mListView.setOnScrollListener(this.adapter);

        this.isOffline = false;
        hideNoConnectionBar();

        loadPage();
    }



    @Override
    public void updateDrawerStatus() {
        ImageView iconReports = (ImageView) findViewById(R.id.sidebar_icon_reports);
        TextView labelSync = (TextView) findViewById(R.id.sidebar_label_reports);

        iconReports.setColorFilter(ContextCompat.getColor(this, R.color.zupblue), PorterDuff.Mode.SRC_ATOP);
        labelSync.setTextColor(ContextCompat.getColor(this, R.color.zupblue));
    }
}

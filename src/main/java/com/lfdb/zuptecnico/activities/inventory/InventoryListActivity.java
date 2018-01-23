package com.lfdb.zuptecnico.activities.inventory;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
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
import com.lfdb.zuptecnico.adapters.InventoryItemsAdapter;
import com.lfdb.zuptecnico.adapters.OfflineInventoryAdapter;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.sync.EditInventoryItemSyncAction;
import com.lfdb.zuptecnico.api.sync.PublishInventoryItemSyncAction;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.fragments.inventory.FilterInventoryFragment;
import com.lfdb.zuptecnico.fragments.inventory.InventoriesMapFragment;
import com.lfdb.zuptecnico.tasks.InventoryItemDownloader;
import com.lfdb.zuptecnico.ui.UIHelper;
import com.lfdb.zuptecnico.util.ItemsAdapterListener;
import com.lfdb.zuptecnico.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryListActivity extends RootActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ItemsAdapterListener, InventoriesMapFragment.Listener {
    private static final int REQUEST_FILTER = 2;
    private static final int REQUEST_CREATE_ITEM = 2;
    FilterInventoryFragment.FilterOptions optionsQuery;

    private ListView mListView;
    protected InventoryItemsAdapter mAdapter;
    private ActionMode mActionMode;

    private Snackbar toast;

    private boolean mIsViewingMap;
    InventoriesMapFragment mMapFragment;
    private View mMapContainer;
    private BroadcastReceiver createdReceiver, editedReceiver;

    private boolean seeDownloadedItems;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_new);
        Zup.getInstance().initStorage(this);
        toast = ZupApplication.toast(findViewById(android.R.id.content), getString(R.string.no_results_found));
        loadUI();
        mListView = (ListView) findViewById(R.id.items_list);
        mAdapter = new InventoryItemsAdapter(this);
        mAdapter.setListener(this);
        mListView.setOnScrollListener(mAdapter);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mMapContainer = findViewById(R.id.inventories_map_container);

        findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOnline();
            }
        });
        findViewById(R.id.inventory_create).setVisibility(Zup.getInstance().getAccess().canCreateInventoryItem() ? View.VISIBLE : View.GONE);
    }

    void loadPage() {
        showLoading();
        mAdapter.reset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(createdReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openInventoryItem((InventoryItem) intent.getParcelableExtra("item"));
            }
        }, new IntentFilter(PublishInventoryItemSyncAction.ITEM_PUBLISHED));

        manager.registerReceiver(editedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openInventoryItem((InventoryItem) intent.getParcelableExtra("item"));
            }
        }, new IntentFilter(EditInventoryItemSyncAction.ITEM_EDITED));

        loadPage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(createdReceiver);
        manager.unregisterReceiver(editedReceiver);
    }

    void showNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
    }

    void hideNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.GONE);
    }

    void showLoading() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_list, menu);
        mMenu = menu;
        refreshMenu();
        return super.onCreateOptionsMenu(menu);
    }

    private void refreshMenu() {
        if (mMenu == null) {
            return;
        }
        mMenu.findItem(R.id.action_map).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_list).setVisible(mIsViewingMap);
        mMenu.findItem(R.id.action_order_creation).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_order_modification).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_order_name).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_search).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_items_filter).setVisible(!mIsViewingMap);
        mMenu.findItem(R.id.action_items_clear_filter).setVisible(mAdapter != null && mAdapter.isFiltered());
    }


    protected void loadUI() {
        UIHelper.setTitle(this, getString(R.string.inventory_items_title));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_map:
                showMap();
                return true;

            case R.id.action_list:
                hideMap();
                return true;

            case R.id.action_search:
                intent = new Intent(this, SearchInventoryByQueryActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_items_filter:
                intent = new Intent(this, FilterInventoryItemsActivity.class);
                if (optionsQuery != null) {
                    intent.putExtra(FilterInventoryItemsActivity.FILTER_OPTIONS, optionsQuery);
                }
                this.startActivityForResult(intent, REQUEST_FILTER);
                return true;
            case R.id.action_items_clear_filter:
                if (mAdapter != null) {
                    mAdapter.setFilterOptions(null);
                    optionsQuery = null;
                    loadPage();
                }
                item.setVisible(false);
                return true;
            case R.id.action_order_creation:
                mAdapter.setOrder(InventoryItemsAdapter.Order.CreationDate);
                return true;

            case R.id.action_order_modification:
                mAdapter.setOrder(InventoryItemsAdapter.Order.ModificationDate);
                return true;

            case R.id.action_order_name:
                mAdapter.setOrder(InventoryItemsAdapter.Order.Title);
                return true;

            case R.id.action_items_viewdownloaded:

                seeDownloadedItems = !seeDownloadedItems;
                if (seeDownloadedItems) {
                    mAdapter = new OfflineInventoryAdapter(this);
                    item.setTitle(R.string.see_all_items);
                } else {
                    mAdapter = new InventoryItemsAdapter(this);
                    item.setTitle(R.string.see_downloaded_items_title);
                    mListView.setOnScrollListener(mAdapter);
                }
                mAdapter.setListener(this);
                mListView.setAdapter(mAdapter);
                loadPage();
                return true;
            case R.id.action_refresh:
                if (mAdapter != null) {
                    mAdapter.reset();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMap() {
        if (mMapFragment == null) {
            mMapFragment = new InventoriesMapFragment();
            mMapFragment.setListener(this);
        }

        mMapContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.inventories_map_container, mMapFragment)
                .commit();

        mIsViewingMap = true;
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

        mIsViewingMap = false;
        refreshMenu();
    }

    public void showCreateDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.new_inventory_item_title));
        InventoryCategory[] categories = Zup.getInstance().getInventoryCategoryService().getInventoryCategories();
        final ArrayList<InventoryCategory> categoriesAllowed = new ArrayList<>();
        if (categories != null && categories.length > 0) {
            int length = categories.length;
            for (int i = 0; i < length; i++) {
                InventoryCategory category = categories[i];
                if (Zup.getInstance().getAccess().canCreateInventoryItem(category.id)) {
                    categoriesAllowed.add(category);
                }
            }

            String[] items = new String[categoriesAllowed.size()];
            for (int i = 0; i < categoriesAllowed.size(); i++) {
                items[i] = categoriesAllowed.get(i).title;
            }

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InventoryCategory category = categoriesAllowed.get(i);
                    dialogInterface.dismiss();

                    Intent intent = new Intent(InventoryListActivity.this, CreateInventoryItemActivity.class);
                    intent.putExtra("create", true);
                    intent.putExtra("categoryId", category.id);
                    startActivityForResult(intent, REQUEST_CREATE_ITEM);
                }
            });

            builder.show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        InventoryItem item = mAdapter.getItem(i);
        if (item == null) {
            return;
        }
        if (mActionMode != null) {
            onItemLongClick(adapterView, view, i, l);
            return;
        }
        openInventoryItem(item);
    }

    @Override
    public void updateDrawerStatus() {
        ImageView iconItems = (ImageView) findViewById(R.id.sidebar_icon_items);
        TextView labelItems = (TextView) findViewById(R.id.sidebar_label_items);
        labelItems.setTextColor(ContextCompat.getColor(this, R.color.zupblue));
        iconItems.setColorFilter(ContextCompat.getColor(this, R.color.zupblue), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null)
            mActionMode = startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " selecionados");
        return true;
    }

    @Override
    public void onItemsLoaded() {
        hideLoading();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            optionsQuery = data.getParcelableExtra(FilterInventoryItemsActivity.FILTER_OPTIONS);
            if (optionsQuery != null) {
                this.mAdapter.setFilterOptions(optionsQuery.getQueryMap());
            }
            showLoading();
        }
        refreshMenu();
    }

    @Override
    public void onEmptyResultsLoaded() {
        hideLoading();
        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast.show();
        }
    }

    @Override
    public void onNetworkError() {
        hideLoading();
        mAdapter = new OfflineInventoryAdapter(this);
        mAdapter.setListener(this);
        mListView.setAdapter(mAdapter);
        showNoConnectionBar();
        loadPage();
    }

    private void goOnline() {
        mAdapter = new InventoryItemsAdapter(this);
        mAdapter.setListener(this);
        mListView.setOnScrollListener(mAdapter);
        mListView.setAdapter(mAdapter);
        hideNoConnectionBar();
        loadPage();
    }

    @Override
    public void openInventoryItem(InventoryItem item) {
        if (item == null) {
            return;
        }
        Intent intent = new Intent(this, InventoryItemDetailsActivity.class);
        intent.putExtra("item_id", item.id);
        intent.putExtra("categoryId", item.inventory_category_id);

        this.startActivityForResult(intent, 0);
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.download_items, menu);
            MenuItem menuItem = menu.findItem(R.id.action_download);
            if (seeDownloadedItems) {
                menuItem.setIcon(R.drawable.ic_action_delete_download);
            } else {
                menuItem.setIcon(R.drawable.ic_action_download);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_download:
                    if (isFinishing()) {
                        return false;
                    }
                    final ProgressDialog dialog = ViewUtils.createProgressDialog(InventoryListActivity.this);
                    toggleSaveItems(mode, dialog);

                    return true;
                default:
                    return false;
            }

        }

        private void toggleSaveItems(final ActionMode mode, final ProgressDialog dialog) {
            if (isFinishing()) {
                return;
            }

            List<InventoryItem> selected = new ArrayList<>(mAdapter
                    .getSelectedIds().values());
            int size = selected.size();
            if (seeDownloadedItems) {
                for (int index = 0; index < size; index++) {
                    InventoryItem mItem = selected.get(index);
                    Zup.getInstance().getInventoryItemService().deleteInventoryItem(mItem.id);
                }
                mode.finish();
            } else {
                dialog.show();
                InventoryItemDownloader download = new InventoryItemDownloader(InventoryListActivity.this, selected,
                        new InventoryItemDownloader.Listener() {
                            @Override
                            public void onProgress(float progress) {
                                if (isFinishing()) {
                                    return;
                                }
                                if (dialog != null) {
                                    dialog.setProgress((int) (progress * 100.0f));
                                }
                            }

                            @Override
                            public void onFinished() {
                                if (isFinishing()) {
                                    return;
                                }
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                mode.finish();
                                refreshMenu();
                            }

                            @Override
                            public void onError() {
                                ZupApplication.toast(findViewById(android.R.id.content),
                                        getString(R.string.error_unable_load_inventory_items)).show();
                            }
                        });
                download.execute();
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.removeSelection();
            mActionMode = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mActionMode != null) {
            mActionMode.finish();
            return;
        }
        super.onBackPressed();
    }
}

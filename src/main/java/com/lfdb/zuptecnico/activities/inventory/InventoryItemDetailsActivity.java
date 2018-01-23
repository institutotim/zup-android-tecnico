package com.lfdb.zuptecnico.activities.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.sync.DeleteInventoryItemSyncAction;
import com.lfdb.zuptecnico.api.sync.EditInventoryItemSyncAction;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.lfdb.zuptecnico.fragments.inventory.InventoryItemGeneralInfoFragment;
import com.lfdb.zuptecnico.fragments.inventory.InventoryItemSectionFragment;
import com.lfdb.zuptecnico.fragments.reports.ReportItemCasesFragment;
import com.lfdb.zuptecnico.fragments.reports.ReportItemMapFragment;
import com.lfdb.zuptecnico.ui.UIHelper;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.RetrofitError;

public class InventoryItemDetailsActivity extends AppCompatActivity {
  private InventoryItem item;
  private InventoryCategory category;
  private Menu menu;
  private boolean isFakeCreate = false;
  private Tasker itemLoader;
  private BroadcastReceiver editedReceiver;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_report_item_details);

    Zup.getInstance().initStorage(getApplicationContext());
    UIHelper.initActivity(this);

    int itemId = getIntent().getIntExtra("item_id", 0);
    int categoryId = getIntent().getIntExtra("categoryId", 0);
    if (getIntent().hasExtra("item")) {
      setItem((InventoryItem) getIntent().getParcelableExtra("item"));
    } else if (Zup.getInstance().getInventoryItemService().hasInventoryItem(itemId)) {
      setItem(Zup.getInstance().getInventoryItemService().getInventoryItem(itemId));
    } else {
      requestItemInfo(itemId, categoryId);
    }

    this.isFakeCreate = getIntent().getBooleanExtra("fake_create", false);

    editedReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        setItem((InventoryItem) intent.getParcelableExtra("item"));
      }
    };

    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.registerReceiver(editedReceiver,
            new IntentFilter(EditInventoryItemSyncAction.ITEM_EDITED));
  }

  private void setItem(InventoryItem item) {
    this.item = item;
    fillItemInfo();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.unregisterReceiver(editedReceiver);
  }

  private void setUpMenu() {
    if (this.menu != null && this.item != null) {
      if (this.item.isLocal) {
        menu.findItem(R.id.action_items_delete_download).setVisible(false);
        menu.findItem(R.id.action_items_download).setVisible(false);
      } else if (Zup.getInstance().getInventoryItemService().hasInventoryItem(this.item.id)) {
        menu.findItem(R.id.action_items_delete_download).setVisible(true);
        menu.findItem(R.id.action_items_download).setVisible(false);
      } else {
        menu.findItem(R.id.action_items_delete_download).setVisible(false);
        menu.findItem(R.id.action_items_download).setVisible(true);
      }

      menu.findItem(R.id.action_items_edit)
              .setVisible(Zup.getInstance().getAccess().canEditInventoryItem(item.inventory_category_id)
                      && !isFakeCreate);

      menu.findItem(R.id.action_items_discard)
              .setVisible(
                      Zup.getInstance().getAccess().canDeleteInventoryItem(item.inventory_category_id));
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.items_details, menu);
    if (this.menu == null && menu != null) this.menu = menu;

    setUpMenu();
    return true;
  }

  void edit() {
    if (!isFakeCreate && Zup.getInstance()
            .getSyncActionService()
            .hasSyncActionRelatedToInventoryItem(this.item.id)) {
      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
      dialogBuilder.setTitle("Ops!");
      dialogBuilder.setMessage(
              "Existe uma sincronização pendente para este item. É necessário concluí-la antes de modificá-lo.");
      dialogBuilder.setCancelable(true);
      dialogBuilder.setNegativeButton("Fechar", null);
      dialogBuilder.show();

      return;
    }

    Intent intent = new Intent(this, CreateInventoryItemActivity.class);
    intent.putExtra("create", false);
    intent.putExtra("categoryId", category.id);
    intent.putExtra("item", this.item);
    startActivity(intent);
    finish();
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (this.item == null) return true;

    switch (item.getItemId()) {
      case R.id.action_items_discard:
        confirmDelete();
        break;
      case R.id.action_items_edit:
        edit();
        break;
      case R.id.action_items_download:
        Zup.getInstance().getInventoryItemService().addInventoryItem(this.item);
        this.menu.findItem(R.id.action_items_delete_download).setVisible(true);
        this.menu.findItem(R.id.action_items_download).setVisible(false);
        break;
      case R.id.action_items_delete_download:
        Zup.getInstance().getInventoryItemService().deleteInventoryItem(this.item.id);
        this.menu.findItem(R.id.action_items_delete_download).setVisible(false);
        this.menu.findItem(R.id.action_items_download).setVisible(true);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void confirmDelete() {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setMessage(R.string.delete_inventory_confirm_text);
    dialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        deleteItem();
      }
    });
    dialogBuilder.setNegativeButton(R.string.cancel, null);
    dialogBuilder.show();
  }

  private void deleteItem() {
    Zup.getInstance()
            .getSyncActionService()
            .addSyncAction(new DeleteInventoryItemSyncAction(item.inventory_category_id, item.id));
    Zup.getInstance().sync();
    finishWithResetSignal();
  }

  void requestItemInfo(int itemId, int categoryId) {
    if (itemLoader != null) itemLoader.cancel(true);

    itemLoader = new Tasker(categoryId, itemId);
    itemLoader.execute();
  }

  private void finishWithResetSignal() {
    this.setResult(2);
    finish();
  }

  void showLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.VISIBLE);
    findViewById(R.id.report_loading).setVisibility(View.VISIBLE);
  }

  void hideLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.GONE);
    findViewById(R.id.report_loading).setVisibility(View.GONE);
  }

  void fillItemInfo() {
    showLoading();
    setUpMenu();

    if (category == null) {
      category = Zup.getInstance()
              .getInventoryCategoryService()
              .getInventoryCategory(item.inventory_category_id);
    }

    if (item != null) {
      UIHelper.setTitle(this, item.title);
    }
    ((ViewGroup) findViewById(R.id.listView)).removeAllViews();
    Fragment[] sections = buildPage();
    hideLoading();

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    if (item != null
            && item.relatedEntities != null
            && item.relatedEntities.cases != null
            && item.relatedEntities.cases.length > 0) {
      ReportItemCasesFragment relatedCases = new ReportItemCasesFragment();
      Bundle casesBundle = new Bundle();
      casesBundle.putParcelableArray("cases", item.relatedEntities.cases);
      relatedCases.setArguments(casesBundle);
      transaction.add(R.id.listView, relatedCases, "related_cases");
    }

    Bundle bundle = new Bundle();
    bundle.putParcelable("item", item);
    InventoryItemGeneralInfoFragment fragment = new InventoryItemGeneralInfoFragment();
    fragment.setArguments(bundle);
    transaction.add(R.id.listView, fragment, "generalInfo");

    int i = 1;
    for (Fragment section : sections) {
      transaction.add(R.id.listView, section, "section" + i);
      i++;
    }

    bundle = new Bundle();
    bundle.putParcelable("inventory", item);
    ReportItemMapFragment mapFragment = new ReportItemMapFragment();
    mapFragment.setArguments(bundle);
    transaction.add(R.id.listView, mapFragment, "mapInfo");

    ReportItemMapFragment mapFragment2 = new ReportItemMapFragment();
    mapFragment2.setArguments(bundle);
    transaction.add(R.id.listView, mapFragment2, "mapInfo2");

    transaction.hide(mapFragment2);

    try {
      transaction.commit();
    } catch (Exception ex) {
      // FIXME Sometimes this will hang and crash after the activity is finished
    }
  }

  private Fragment[] buildPage() {
    ArrayList<Fragment> result = new ArrayList<Fragment>();
    if (category.sections != null) {
      Arrays.sort(category.sections);

      for (int i = 0; i < category.sections.length; i++) {
        InventoryCategory.Section section = category.sections[i];
        if (!Zup.getInstance().getAccess().canViewInventorySection(category.id, section.id)) {
          continue;
        }
        if (section.disabled) {
          continue;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        bundle.putSerializable("section", section);
        InventoryItemSectionFragment fragment = new InventoryItemSectionFragment();
        fragment.setArguments(bundle);
        result.add(fragment);
      }
    }
    Fragment[] resultarr = new Fragment[result.size()];
    result.toArray(resultarr);
    return resultarr;
  }

  class Tasker extends AsyncTask<Void, Void, InventoryItem> {
    int catId;
    int itemId;

    public Tasker(int categoryId, int itemId) {
      this.catId = categoryId;
      this.itemId = itemId;
    }

    @Override protected void onPreExecute() {
      showLoading();
    }

    @Override protected InventoryItem doInBackground(Void... voids) {
      try {
        SingleInventoryItemCollection result =
                Zup.getInstance().getService().getInventoryItem(catId, itemId);
        if (result == null) return null;

        return result.item;
      } catch (RetrofitError error) {
        Log.e("Retrofit", "Failed to load inventory item", error);
        return null;
      }
    }

    @Override protected void onPostExecute(InventoryItem inventoryItem) {
      if (inventoryItem == null) {
        ZupApplication.toast(findViewById(android.R.id.content), R.string.error_loading_item)
                .show();
      } else {
        setItem(inventoryItem);
      }
    }
  }
}

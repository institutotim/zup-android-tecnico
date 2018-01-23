package com.lfdb.zuptecnico.activities.inventory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.sync.EditInventoryItemSyncAction;
import com.lfdb.zuptecnico.api.sync.PublishInventoryItemSyncAction;
import com.lfdb.zuptecnico.api.sync.PublishOrEditInventorySyncAction;
import com.lfdb.zuptecnico.api.sync.SyncAction;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryCategoryStatus;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.fragments.inventory.CreateInventoryItemSectionFragment;
import com.lfdb.zuptecnico.fragments.inventory.CreateInventoryItemStatusFragment;
import com.lfdb.zuptecnico.fragments.inventory.CreateInventoryPublisher;
import com.lfdb.zuptecnico.ui.UIHelper;
import com.lfdb.zuptecnico.util.Utilities;
import com.lfdb.zuptecnico.util.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateInventoryItemActivity extends AppCompatActivity {
  public static final int FAKE_MIN_ID = 999999;
  boolean createMode;
  InventoryCategory category;
  InventoryItem item;
  SyncAction action;
  private boolean isFakeCreate = false;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inventory_item_create);

    showLoadingBar();
    Zup.getInstance().initStorage(getApplicationContext());

    UIHelper.initActivity(this);
    Intent intent = getIntent();

    isFakeCreate = intent.getBooleanExtra("fake_create", false);

    createMode = intent.getBooleanExtra("create", true);
    action = intent.getParcelableExtra("action");
    if (action != null) {
      item = ((PublishOrEditInventorySyncAction) action).item;
      createMode = false;
      isFakeCreate = true;
      category = Zup.getInstance()
          .getInventoryCategoryService()
          .getInventoryCategory(item.inventory_category_id);
      fillItemInfo();
      return;
    }
    if (createMode) {
      int categoryId = intent.getIntExtra("categoryId", 0);
      category = Zup.getInstance().getInventoryCategoryService().getInventoryCategory(categoryId);
    } else {
      int categoryId = intent.getIntExtra("categoryId", 0);
      int itemId = intent.getIntExtra("item_id", 0);
      item = intent.getParcelableExtra("item");
      category = Zup.getInstance().getInventoryCategoryService().getInventoryCategory(categoryId);
      if (item == null) {
        if (Zup.getInstance().getInventoryItemService().hasInventoryItem(itemId)) {
          item = Zup.getInstance().getInventoryItemService().getInventoryItem(itemId);
        } else {
          ZupApplication.toast(findViewById(android.R.id.content),
              R.string.error_loading_report_item).show();
          finish();
          return;
        }
      }
    }
    if (category != null) {
      fillItemInfo();
    } else {
      ZupApplication.toast(findViewById(android.R.id.content),
          R.string.error_loading_inventory_category).show();
      finish();
    }
  }

  @Override protected void onResume() {
    super.onResume();

  }

  void fillItemInfo() {
    showLoadingBar();
    ((ViewGroup) findViewById(R.id.inventory_item_create_container)).removeAllViews();
    Fragment[] sections = buildPage();
    hideLoadingBar();

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    int i = 1;
    for (Fragment section : sections) {
      transaction.add(R.id.inventory_item_create_container, section, "section" + i);
      i++;
    }

    try {
      transaction.commit();
    } catch (Exception ex) {
      // FIXME Sometimes this will hang and crash after the activity is finished
    }
  }

  class InventoryPublisher extends AsyncTask<Void, Void, InventoryItem> {
    ProgressDialog dialog;
    boolean requiredFieldsAreFilled;

    @Override protected void onPreExecute() {
      super.onPreExecute();
      if (isFinishing()) {
        return;
      }
      dialog = ViewUtils.createProgressDialog(CreateInventoryItemActivity.this);
      dialog.setMessage(getString(R.string.validating_data_dialog_message));
      dialog.show();
      requiredFieldsAreFilled = validateFields();
      dialog.setMessage(getString(R.string.creating_item_dialog_message));
    }

    @Override protected InventoryItem doInBackground(Void... voids) {
      if (!requiredFieldsAreFilled || isFinishing()) {
        return null;
      }

      return createItemFromData();
    }

    private Boolean validateFields() {
      boolean validationFailed = false;
      View firstFieldError = null;

      FragmentManager manager = getSupportFragmentManager();
      List<Fragment> sections = manager.getFragments();
      int sectionsCount = sections.size();
      for (int i = 0; i < sectionsCount; i++) {
        CreateInventoryPublisher fragment = (CreateInventoryPublisher) sections.get(i);
        if (fragment == null) {
          continue;
        }
        firstFieldError = fragment.validateSection(firstFieldError);
        if (firstFieldError != null) {
          validationFailed = true;
        }
        final int progress = 100 * (i + 1) / sectionsCount * 2;
        runOnUiThread(new Runnable() {
          @Override public void run() {
            if (isFinishing()) {
              return;
            }
            if (dialog != null && dialog.isShowing()) {
              dialog.setProgress(progress);
            }
          }
        });
      }
      if (validationFailed) {
        if (dialog != null && dialog.isShowing()) {
          dialog.dismiss();
        }
      }

      return !validationFailed;
    }

    @Override protected void onPostExecute(InventoryItem result) {
      super.onPostExecute(result);
      if (result == null || isFinishing()) {
        return;
      }
      item = result;
      item.inventory_category_id = category.id;
      if (Zup.getInstance().getInventoryItemService().hasInventoryItem(item.id)) {
        Zup.getInstance().getInventoryItemService().addInventoryItem(item);
      }

      if (action != null) {
        if (action instanceof PublishInventoryItemSyncAction) {
          PublishInventoryItemSyncAction mAction = (PublishInventoryItemSyncAction) action;
          mAction.item = item;
          Zup.getInstance().updateSyncAction(mAction);
        } else {
          EditInventoryItemSyncAction mAction = (EditInventoryItemSyncAction) action;
          mAction.item = item;
          Zup.getInstance().updateSyncAction(mAction);
        }
      } else {
        if (createMode) {
          Zup.getInstance()
              .getSyncActionService()
              .addSyncAction(new PublishInventoryItemSyncAction(item));
        } else {
          Zup.getInstance()
              .getSyncActionService()
              .addSyncAction(new EditInventoryItemSyncAction(item));
        }
      }
      if (dialog != null && dialog.isShowing()) {
        dialog.dismiss();
      }

      if (Utilities.isConnected(CreateInventoryItemActivity.this)) {
        Zup.getInstance().sync();
      }

      if (!Utilities.isConnected(CreateInventoryItemActivity.this)) {
        Intent intent;
        intent = new Intent(CreateInventoryItemActivity.this, InventoryItemDetailsActivity.class);
        intent.putExtra("item", item);
        intent.putExtra("fake_create", true);
        startActivity(intent);
      }
      finish();
    }

    private InventoryItem createItemFromData() {
      InventoryItem item;
      if (createMode) {
        item = Zup.getInstance().createInventoryItem();
      } else {
        item = CreateInventoryItemActivity.this.item;
      }

      FragmentManager manager = getSupportFragmentManager();
      List<Fragment> sections = manager.getFragments();
      int sectionsCount = sections.size();
      for (int i = 0; i < sectionsCount; i++) {
        CreateInventoryPublisher fragment = (CreateInventoryPublisher) sections.get(i);
        if (fragment == null) {
          continue;
        }
        item = fragment.createItemFromData(item);
        int progress = 50 + 100 * (i + 1) / sectionsCount * 2;
        dialog.setProgress(progress);
      }
      return item;
    }
  }

  public void finishEditing(View view) {
    view.requestFocus();
    new InventoryPublisher().execute();
  }

  void showLoadingBar() {
    findViewById(R.id.loading).setVisibility(View.VISIBLE);
    findViewById(R.id.inventory_item_create_scroll).setVisibility(View.GONE);
    findViewById(R.id.toolbar).setVisibility(View.GONE);
  }

  void hideLoadingBar() {
    findViewById(R.id.loading).setVisibility(View.GONE);
    findViewById(R.id.inventory_item_create_scroll).setVisibility(View.VISIBLE);
    findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
  }

  Fragment[] buildPage() {
    ArrayList<Fragment> result = new ArrayList<Fragment>();
    if (category.sections != null) {
      Arrays.sort(category.sections);

      InventoryCategoryStatus[] statuses = category.statuses;
      if (statuses != null && statuses.length > 0) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        bundle.putSerializable("category", category);
        bundle.putBoolean("create_mode", createMode);
        CreateInventoryItemStatusFragment statusFragment = new CreateInventoryItemStatusFragment();
        statusFragment.setArguments(bundle);
        result.add(statusFragment);
      }

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
        bundle.putSerializable("category", category);
        bundle.putBoolean("create_mode", createMode);
        bundle.putSerializable("section", section);
        CreateInventoryItemSectionFragment fragment = new CreateInventoryItemSectionFragment();
        fragment.setArguments(bundle);
        result.add(fragment);
      }
    }

    Fragment[] resultarr = new Fragment[result.size()];
    result.toArray(resultarr);
    return resultarr;
  }
}

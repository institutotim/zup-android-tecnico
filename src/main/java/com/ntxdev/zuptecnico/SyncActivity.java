package com.ntxdev.zuptecnico;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.ntxdev.zuptecnico.activities.RootActivity;
import com.ntxdev.zuptecnico.activities.cases.ViewCaseStepFormActivity;
import com.ntxdev.zuptecnico.activities.inventory.CreateInventoryItemActivity;
import com.ntxdev.zuptecnico.activities.inventory.InventoryItemDetailsActivity;
import com.ntxdev.zuptecnico.activities.reports.CreateReportItemActivity;
import com.ntxdev.zuptecnico.activities.reports.ReportItemDetailsActivity;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.sync.ChangeReportResponsableGroupSyncAction;
import com.ntxdev.zuptecnico.api.sync.ChangeReportResponsableUserSyncAction;
import com.ntxdev.zuptecnico.api.sync.ChangeReportStatusSyncAction;
import com.ntxdev.zuptecnico.api.sync.DeleteCaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.DeleteInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.DeleteReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.EditInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.FillCaseStepSyncAction;
import com.ntxdev.zuptecnico.api.sync.FinishCaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishOrEditInventorySyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishReportCommentSyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.SyncAction;
import com.ntxdev.zuptecnico.config.Constants;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.collections.SingleCaseCollection;
import com.ntxdev.zuptecnico.entities.requests.UpdateCaseStepRequest;
import com.ntxdev.zuptecnico.util.BitmapUtil;
import com.ntxdev.zuptecnico.util.Utilities;
import com.ntxdev.zuptecnico.util.ViewUtils;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SyncActivity extends RootActivity {
  Menu mMenu;
  private Receiver changedAction;
  private Receiver endAction;
  private Receiver beginAction;

  @Override public void updateDrawerStatus() {
    TextView labelSync = (TextView) findViewById(R.id.sidebar_label_sync);
    ImageView iconSync = (ImageView) findViewById(R.id.sidebar_icon_sync);

    labelSync.setTextColor(ContextCompat.getColor(this, R.color.zupblue));
    iconSync.setColorFilter(ContextCompat.getColor(this, R.color.zupblue),
        PorterDuff.Mode.SRC_ATOP);
  }

  class Receiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      SyncActivity.this.onReceive(intent);
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_sync);
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

    manager.registerReceiver(beginAction = new Receiver(),
        new IntentFilter(SyncAction.ACTION_SYNC_BEGIN));
    manager.registerReceiver(endAction = new Receiver(),
        new IntentFilter(SyncAction.ACTION_SYNC_END));
    manager.registerReceiver(changedAction = new Receiver(),
        new IntentFilter(SyncAction.ACTION_SYNC_CHANGED));
    this.fillItems();
  }

  @Override protected void onResume() {
    super.onResume();
    if (getIntent() != null && getIntent().getBooleanExtra(SYNC_NOW, false)) {
      if (Utilities.isConnected(this)) {
        Zup.getInstance().sync();
      } else {
        ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
            .show();
      }
    }
  }

  @Override protected void onDestroy() {
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
    manager.unregisterReceiver(beginAction);
    manager.unregisterReceiver(endAction);
    manager.unregisterReceiver(changedAction);
    super.onDestroy();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sync, menu);

    this.mMenu = menu;

    if (Zup.getInstance().isSyncing()) hideButton();

    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_sync:
        if (Utilities.isConnected(this)) {
          Zup.getInstance().sync();
        } else {
          ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
              .show();
        }
        return true;
      case R.id.action_clear:
        Zup.getInstance().getSyncActionService().clearSuccessfulItems();
        ZupApplication.toast(findViewById(android.R.id.content),
            R.string.successfull_items_cleared_message).show();
        this.fillItems();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void onReceive(Intent intent) {
    if (intent.getAction().equals(SyncAction.ACTION_SYNC_CHANGED)) {
      fillItems();
    } else if (intent.getAction().equals(SyncAction.ACTION_SYNC_BEGIN)) {
      hideButton();
    } else if (intent.getAction().equals(SyncAction.ACTION_SYNC_END)) {
      showButton();
    }
  }

  void showButton() {
    if (mMenu == null) {
      return;
    }
    MenuItem item = mMenu.findItem(R.id.action_sync);
    MenuItem clearItem = mMenu.findItem(R.id.action_clear);
    item.setVisible(true);
    clearItem.setVisible(true);
  }

  void hideButton() {
    if (mMenu == null) {
      return;
    }
    MenuItem item = mMenu.findItem(R.id.action_sync);
    MenuItem clearItem = mMenu.findItem(R.id.action_clear);
    item.setVisible(false);
    clearItem.setVisible(false);
  }

  void fillItems() {
    ViewGroup container = (ViewGroup) findViewById(R.id.activity_sync_list);
    container.removeAllViews();

    boolean hasAny = false;

    List<SyncAction> actionIterator = Zup.getInstance().getSyncActionService().getSyncActions();
    int size = actionIterator.size();
    for (int i = 0; i < size; i++) {
      SyncAction action = actionIterator.get(i);
      View view = setupItemView(action);

      container.addView(view);

      hasAny = true;
    }

    findViewById(R.id.activity_sync_none).setVisibility(!hasAny ? View.VISIBLE : View.GONE);
  }

  View setupItemView(final SyncAction action) {
    View view = getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);

    TextView textTitle = (TextView) view.findViewById(R.id.fragment_inventory_item_title);
    TextView textDescription = (TextView) view.findViewById(R.id.fragment_inventory_item_desc);
    TextView stateDesc = (TextView) view.findViewById(R.id.fragment_inventory_item_statedesc);

    String itemId = "";

    if (action instanceof PublishInventoryItemSyncAction) {
      PublishInventoryItemSyncAction publish = (PublishInventoryItemSyncAction) action;
      InventoryCategory category = Zup.getInstance()
          .getInventoryCategoryService()
          .getInventoryCategory(publish.item.inventory_category_id);
      itemId = publish.item.title;
      textTitle.setText(getString(R.string.create_inventory_item_title));
      if (category != null) {
        textDescription.setText(category.title);
      }
    } else if (action instanceof EditInventoryItemSyncAction) {
      EditInventoryItemSyncAction edit = (EditInventoryItemSyncAction) action;
      itemId = edit.item.title;
      textTitle.setText(getString(R.string.edit_inventory_item_title));
      textDescription.setText(edit.item.title);
    } else if (action instanceof DeleteInventoryItemSyncAction) {
      DeleteInventoryItemSyncAction delete = (DeleteInventoryItemSyncAction) action;
      InventoryCategory category =
          Zup.getInstance().getInventoryCategoryService().getInventoryCategory(delete.categoryId);
      textTitle.setText(getString(R.string.delete_inventory_item_title));
      textDescription.setText(
          getString(R.string.id_title).toUpperCase() + ": " + delete.itemId + ", " + getString(
              R.string.category_title) + ": " + category.title);
    } else if (action instanceof FillCaseStepSyncAction) {
      FillCaseStepSyncAction fill = (FillCaseStepSyncAction) action;
      textTitle.setText(getString(R.string.fill_case_step));
      textDescription.setText(getString(R.string.case_title)
          + " #"
          + fill.caseId
          + " "
          + getString(R.string.step_title)
          + " #"
          + fill.stepId);
    } else if (action instanceof PublishReportItemSyncAction) {
      PublishReportItemSyncAction publish = (PublishReportItemSyncAction) action;
      itemId = "#" + String.valueOf(publish.inventoryItemId);
      ReportCategory category =
          Zup.getInstance().getReportCategoryService().getReportCategory(publish.categoryId);

      textTitle.setText(getString(R.string.activity_title_create_report_item));
      if (category != null) {
        textDescription.setText(category.title);
      } else {
        textDescription.setText(getString(R.string.invalid_category));
      }
    } else if (action instanceof DeleteReportItemSyncAction) {
      DeleteReportItemSyncAction delete = (DeleteReportItemSyncAction) action;
      itemId = "#" + String.valueOf(delete.itemId);
      textTitle.setText(getString(R.string.delete_report_title));
      textDescription.setText(String.valueOf(delete.itemId));
    } else if (action instanceof EditReportItemSyncAction) {
      EditReportItemSyncAction edit = (EditReportItemSyncAction) action;
      itemId = "#" + String.valueOf(edit.reportId);
      ReportCategory category =
          Zup.getInstance().getReportCategoryService().getReportCategory(edit.categoryId);

      textTitle.setText(getString(R.string.edit_report_title));
      if (category != null) {
        textDescription.setText(category.title);
      } else {
        textDescription.setText(getString(R.string.invalid_category));
      }
    } else if (action instanceof PublishReportCommentSyncAction) {
      PublishReportCommentSyncAction publish = (PublishReportCommentSyncAction) action;
      itemId = "#" + String.valueOf(publish.itemId);
      textTitle.setText(getString(R.string.create_comment_report_title));
      textDescription.setText(publish.message);
    } else if (action instanceof ChangeReportResponsableGroupSyncAction) {
      textTitle.setText(R.string.change_responsable_group_text);
    } else if (action instanceof ChangeReportResponsableUserSyncAction) {
      textTitle.setText(R.string.change_responsable_user_text);
    } else if (action instanceof FinishCaseSyncAction) {
      FinishCaseSyncAction syncAction = (FinishCaseSyncAction) action;
      textTitle.setText(R.string.finish_case_title);
      textDescription.setText(getString(R.string.case_number) + syncAction.caseId);
    } else if (action instanceof DeleteCaseSyncAction) {
      DeleteCaseSyncAction syncAction = (DeleteCaseSyncAction) action;
      textTitle.setText(R.string.delete_caso_title);
      textDescription.setText(getString(R.string.case_number) + syncAction.itemId);
    } else if (action instanceof ChangeReportStatusSyncAction) {
      textTitle.setText(R.string.change_report_status);
      itemId = "#" + String.valueOf(((ChangeReportStatusSyncAction) action).itemId);
      ReportCategory category = Zup.getInstance()
          .getReportCategoryService()
          .getReportCategory(((ChangeReportStatusSyncAction) action).categoryId);
      if (category != null) {
        textDescription.setText(category.title);
      } else {
        textDescription.setText(getString(R.string.invalid_category));
      }
    }

    int color;
    String text;
    if (action.isPending()) {
      text = getString(R.string.pending_title);
      color = ContextCompat.getColor(this, R.color.pending_action_color);

      view.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          showDetails(action);
        }
      });
      view.setClickable(true);
    } else if (action.isRunning()) {
      text = getString(R.string.in_execution);
      color = ContextCompat.getColor(this, R.color.running_action_color);
    } else if (action.wasSuccessful()) {
      text = getString(R.string.done);
      if (!TextUtils.isEmpty(itemId)) {
        String title = textTitle.getText().toString();
        textTitle.setText(title + " " + itemId);
      }
      color = ContextCompat.getColor(this, R.color.completed_action_color);
      view.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          if (action instanceof EditInventoryItemSyncAction
              || action instanceof PublishInventoryItemSyncAction) {
            showItem(action);
          } else {
            showReportItem(action);
          }
        }
      });
    } else {
      text = getString(R.string.error_title);
      color = ContextCompat.getColor(this, R.color.error_action_color);

      view.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          showError(action);
        }
      });
      view.setClickable(true);
      if (action instanceof EditReportItemSyncAction
          && action.getError() != null
          && action.getError().equals(SyncAction.NOT_FOUND_ERROR)) {
        askAboutNotFoundReport((EditReportItemSyncAction) action);
      } else if (action instanceof EditInventoryItemSyncAction
          && action.getError() != null
          && action.getError().equals(SyncAction.NOT_FOUND_ERROR)) {
        askAboutNotFoundInventoryItem((EditInventoryItemSyncAction) action);
      }
    }

    stateDesc.setText(text);
    stateDesc.setBackgroundColor(color);

    return view;
  }

  private void askAboutNotFoundInventoryItem(final EditInventoryItemSyncAction action) {
    final InventoryItem item = action.item;

    AlertDialog.Builder builder =
        new AlertDialog.Builder(getParent() == null ? SyncActivity.this : getParent());
    builder.setTitle(getString(R.string.error_title));
    builder.setMessage(
        getString(R.string.not_found_inventory_item_message_header) + action.item.title + getString(
            R.string.not_found_inventory_footer));
    builder.setPositiveButton(getString(R.string.save_new_inventory),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            PublishInventoryItemSyncAction publishAction = new PublishInventoryItemSyncAction(item);
            Zup.getInstance().getSyncActionService().addSyncAction(publishAction);
            Zup.getInstance().performSyncAction(publishAction);
            cancelConfirm(action);
          }
        });
    builder.setNegativeButton(getString(R.string.undo_changes),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            cancelConfirm(action);
          }
        });
    builder.create().show();
  }

  private void askAboutNotFoundReport(final EditReportItemSyncAction action) {
    final ReportItem item = Zup.getInstance().getReportItemService().getReportItem(action.reportId);
    if (item != null) {
      return;
    }
    AlertDialog.Builder builder =
        new AlertDialog.Builder(getParent() == null ? SyncActivity.this : getParent());
    builder.setTitle(getString(R.string.error_title));
    builder.setMessage(
        getString(R.string.not_found_report_message_header) + action.reportId + getString(
            R.string.not_found_report_footer));
    builder.setPositiveButton(getString(R.string.save_new_report),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {

            ReportSaviorTask task = new ReportSaviorTask(action, item);
            AsyncTaskCompat.executeParallel(task);
          }
        });
    builder.setNegativeButton(getString(R.string.undo_changes),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            cancelConfirm(action);
          }
        });
    builder.create().show();
  }

  void editItem(final SyncAction action) {
    Intent intent = new Intent(this, CreateReportItemActivity.class);
    intent.putExtra("action", action);
    startActivity(intent);
    return;
  }

  InventoryItem getInventoryItem(SyncAction action) {
    if (action instanceof PublishOrEditInventorySyncAction) {
      return ((PublishOrEditInventorySyncAction) action).item;
    }
    return null;
  }

  void showReportItem(final SyncAction action) {
    if (action instanceof EditReportItemSyncAction) {
      EditReportItemSyncAction mAction = (EditReportItemSyncAction) action;
      Intent intent = new Intent(this, ReportItemDetailsActivity.class);
      intent.putExtra("item_id", mAction.reportId);
      startActivity(intent);
    } else if (action instanceof PublishReportItemSyncAction) {
      PublishReportItemSyncAction mAction = (PublishReportItemSyncAction) action;
      Intent intent = new Intent(this, ReportItemDetailsActivity.class);
      intent.putExtra("item_id", mAction.inventoryItemId);
      startActivity(intent);
    }
  }

  void showItem(final SyncAction action) {
    InventoryItem item = getInventoryItem(action);
    if (item == null) {
      return;
    }

    Intent intent = new Intent(this, InventoryItemDetailsActivity.class);
    if (action.wasSuccessful()) {
      intent.putExtra("item_id", item.id);
      intent.putExtra("categoryId", item.inventory_category_id);
    } else {
      intent.putExtra("item", item);
    }
    intent.putExtra("fake_create", true);
    this.startActivityForResult(intent, 0);
    this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }

  void showErrorMessage(final SyncAction action) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.error_title));
    builder.setMessage(getString(R.string.date_title) + ": " + Zup.getInstance()
        .getDateFormat()
        .format(action.getDate()) + "\r\n\r\n" + action.getError());
    builder.create().show();
  }

  void showDetails(final SyncAction action) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.action_details));

    final boolean isItems = action instanceof PublishInventoryItemSyncAction
        || action instanceof EditInventoryItemSyncAction;
    final boolean isReportItems =
        action instanceof PublishReportItemSyncAction || action instanceof EditReportItemSyncAction;

    String[] defaultItems = new String[] {
        getString(R.string.send_to_system_title), getString(R.string.cancel_action),
        getString(R.string.close_title)
    };
    if (isItems || isReportItems) {
      defaultItems = new String[] {
          getString(R.string.send_to_system_title), getString(R.string.cancel_action),
          getString(R.string.view_item_title), getString(R.string.close_title)
      };
    }
    final Context context = this;
    builder.setItems(defaultItems, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        if (i == 0) {
          tryAgain(action);
        } else if (i == 1) {
          cancel(action);
        } else if (i == 2) {
          if (isItems) {
            showItem(action);
          } else if (isReportItems) {
            showReportDetails(action);
          } else {
            dialogInterface.dismiss();
          }
        }
      }
    });

    builder.create().show();
  }

  private void showReportDetails(SyncAction action) {
    Intent intent = new Intent(this, ReportItemDetailsActivity.class);
    intent.putExtra("action", action);
    startActivity(intent);
    return;
  }

  void showError(final SyncAction action) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.error_title));

    final boolean isItems = action instanceof PublishInventoryItemSyncAction
        || action instanceof EditInventoryItemSyncAction;
    final boolean isReportItems =
        action instanceof PublishReportItemSyncAction || action instanceof EditReportItemSyncAction;

    String[] defaultItems = new String[] {
        getString(R.string.see_message), getString(R.string.try_again),
        getString(R.string.cancel_action), getString(R.string.edit_item_title),
        getString(R.string.close_title)
    };
    final Context context = this;
    builder.setItems(defaultItems, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
          case 0:
            showErrorMessage(action);
            break;
          case 1:
            tryAgain(action);
            break;
          case 2:
            cancel(action);
            break;
          case 3:
            if (isItems) {
              editLocation(action, context);
            } else if (isReportItems) {
              editItem(action);
            } else {
              editStep(action);
            }
            break;
          default:
            break;
        }
      }
    });

    builder.create().show();
  }

  private void editStep(SyncAction action) {
    if (!(action instanceof FillCaseStepSyncAction)) {
      return;
    }
    final FillCaseStepSyncAction fillCaseStepSyncAction = (FillCaseStepSyncAction) action;
    final ProgressDialog dialog = ViewUtils.createProgressDialog(this);
    Zup.getInstance()
        .getService()
        .retrieveCase(fillCaseStepSyncAction.caseId, new Callback<SingleCaseCollection>() {
          @Override
          public void success(SingleCaseCollection singleCaseCollection, Response response) {
            Case flowCase = singleCaseCollection.flowCase;
            if (flowCase == null) {
              showError(null);
              return;
            }
            int stepId = fillCaseStepSyncAction.stepId;
            final Case.Step caseStep = flowCase.getStep(stepId);
            if (caseStep == null) {
              showError(null);
              return;
            }
            if (caseStep.caseStepDataFields == null) {
              caseStep.caseStepDataFields = new ArrayList<Case.Step.DataField>();
            }
            for (UpdateCaseStepRequest.FieldValue field : fillCaseStepSyncAction.fields) {
              Case.Step.DataField data = new Case.Step.DataField();
              data.id =
                  stepId * field.id * (new Random(System.currentTimeMillis()).nextInt(1337) + 1);

              data.fieldId = field.id;
              data.value = field.value;

              caseStep.caseStepDataFields.add(data);
            }
            dialog.dismiss();

            Intent intent = new Intent(SyncActivity.this, ViewCaseStepFormActivity.class);
            intent.putExtra("case", flowCase);
            intent.putExtra("stepId", stepId);
            intent.putExtra("action", fillCaseStepSyncAction.getId());
            startActivity(intent);
          }

          @Override public void failure(RetrofitError error) {
            showError(error);
          }

          private void showError(Exception error) {
            dialog.dismiss();
            if (error != null) {
              error.printStackTrace();
              Crashlytics.logException(error);
            }
            Toast.makeText(SyncActivity.this, R.string.error_impossible_load_case,
                Toast.LENGTH_LONG).show();
          }
        });
    dialog.show();
  }

  void tryAgain(SyncAction action) {
    if (Utilities.isConnected(this)) {
      Zup.getInstance().performSyncAction(action);
    } else {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
          .show();
    }
  }

  void editLocation(SyncAction action, Context context) {
    InventoryItem item = getInventoryItem(action);
    Intent intent = new Intent(this, CreateInventoryItemActivity.class);
    intent.putExtra("create", action instanceof PublishInventoryItemSyncAction);
    intent.putExtra("categoryId", item.inventory_category_id);
    intent.putExtra("action", action);
    intent.putExtra("fake_create", true);
    startActivityForResult(intent, 0);
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }

  void cancel(final SyncAction action) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.warning).toUpperCase());
    builder.setMessage(getString(R.string.cancel_sync_warning_message));
    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        cancelConfirm(action);
      }
    });
    builder.setNegativeButton(getString(R.string.no), null);
    builder.show();
  }

  void cancelConfirm(SyncAction action) {
    if (action instanceof PublishOrEditInventorySyncAction) {
      PublishOrEditInventorySyncAction publishInventoryItemSyncAction =
          (PublishOrEditInventorySyncAction) action;
      Zup.getInstance()
          .getInventoryItemService()
          .deleteInventoryItem(publishInventoryItemSyncAction.item.id);
    } else if (action instanceof EditReportItemSyncAction) {
      Zup.getInstance().getReportItemService().deleteReportItem(action.getId());
    }
    Zup.getInstance().getSyncActionService().removeSyncAction(action.getId());
    fillItems();
  }

  public class ReportSaviorTask extends AsyncTask<Void, Void, ReportItem.Image[]> {
    ReportItem item;
    EditReportItemSyncAction action;

    public ReportSaviorTask(EditReportItemSyncAction action, ReportItem item) {
      this.action = action;
      this.item = item;
    }

    @Override protected ReportItem.Image[] doInBackground(Void... imageViews) {
      int size = 0;
      if (item != null && item.images != null && action != null && action.images != null) {
        size = item.images.length + action.images.length;
      } else if (item != null && item.images != null) {
        size = item.images.length;
      } else if (action != null && action.images != null) {
        size = action.images.length;
      }
      ReportItem.Image[] encodedImages = new ReportItem.Image[size];
      if (item != null && item.images != null) {
        encodedImages = getDownloadedItemImages(size);
      }
      int actionIndex = 0;
      if (action != null && action.images != null) {
        for (int index = item.images.length; index < size; index++) {
          encodedImages[index] = action.images[actionIndex];
          actionIndex++;
        }
      }
      return encodedImages;
    }

    private ReportItem.Image[] getDownloadedItemImages(int size) {
      ReportItem.Image[] encodedImages = new ReportItem.Image[size];
      for (int index = 0; index < item.images.length; index++) {
        encodedImages[index] = new ReportItem.Image();
        try {
          int lastNameIndex = item.images[index].original.lastIndexOf('/');
          String filename = item.images[index].original.substring(lastNameIndex);
          encodedImages[index].setFilename(filename);
          encodedImages[index].setContent(BitmapUtil.convertToBase64(
              Picasso.with(getApplicationContext()).load(item.images[index].original).get()));
        } catch (IOException e) {
          Log.e("Error Picasso Image", e.getMessage(), e.getCause());
          encodedImages[index].setContent("");
        }
      }
      return encodedImages;
    }

    @Override protected void onPostExecute(ReportItem.Image[] result) {
      SyncAction newReportAction =
          new PublishReportItemSyncAction(Constants.DEFAULT_LAT, Constants.DEFAULT_LON,
              action.categoryId, action.description, action.reference, action.address,
              action.number, action.postalCode, action.district, action.city, action.state,
              action.country, result,
              Zup.getInstance().getUserService().getUser(Zup.getInstance().getSessionUserId()),
              action.custom_fields);
      Zup.getInstance().getSyncActionService().addSyncAction(newReportAction);
      Zup.getInstance().performSyncAction(newReportAction);
      cancelConfirm(action);
    }
  }
}

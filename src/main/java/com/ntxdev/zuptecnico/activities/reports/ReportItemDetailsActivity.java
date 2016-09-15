package com.ntxdev.zuptecnico.activities.reports;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.adapters.ReportItemCommentsAdapter;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.sync.ChangeReportResponsableGroupSyncAction;
import com.ntxdev.zuptecnico.api.sync.ChangeReportResponsableUserSyncAction;
import com.ntxdev.zuptecnico.api.sync.DeleteReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishReportCommentSyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.SyncAction;
import com.ntxdev.zuptecnico.entities.ReportHistoryItem;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.ReportNotificationCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemCasesFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemCommentsFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemExtraInfoFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemGeneralInfoFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemHistoryFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemImagesFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemMapFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemNotificationFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemUserInfoFragment;
import com.ntxdev.zuptecnico.tasks.ReportItemDownloader;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.Utilities;
import com.ntxdev.zuptecnico.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReportItemDetailsActivity extends AppCompatActivity
    implements Callback<SingleReportItemCollection> {
  public static final int REQUEST_EDIT_REPORT = 1;
  public static final int RESULT_DELETED = 3;
  public static final int RESULT_CHANGED = 2;

  ReportItemGeneralInfoFragment generalInfo;
  ReportItemCasesFragment relatedCases;
  ReportItemImagesFragment images;
  ReportItemMapFragment map;
  ReportItemNotificationFragment notificationInfo;
  ReportItemUserInfoFragment userInfo;
  ReportItemCommentsFragment comments;
  ReportItemCommentsFragment internalComments;
  ReportItemHistoryFragment history;
  ReportItemExtraInfoFragment extraInfo;
  Bundle bundle;
  ProgressDialog dialog;

  BroadcastReceiver createdReceiver;
  BroadcastReceiver changedResponsableUserReceiver;
  BroadcastReceiver changedResponsableGroupReceiver;
  private SyncAction action;
  Menu menu;
  private BroadcastReceiver editedReceiver;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_report_item_details);
    UIHelper.initActivity(this);
    ReportItem item = getIntent().getParcelableExtra("item");
    action = getIntent().getParcelableExtra("action");
    bundle = savedInstanceState != null ? savedInstanceState : new Bundle();
    if (action != null) {
      bundle.putParcelable("action", action);
      if (action instanceof PublishReportItemSyncAction) {
        item = ((PublishReportItemSyncAction) action).convertToReportItem();
      } else if (action instanceof EditReportItemSyncAction) {
        item = ((EditReportItemSyncAction) action).convertToReportItem();
      }
    }
    if (item != null) {
      bundle.putParcelable("item", item);
      itemLoaded();
    } else {
      int itemId = getIntent().getIntExtra("item_id", -1);
      if (!Utilities.isConnected(this) && Zup.getInstance()
          .getReportItemService()
          .hasReportItem(itemId)) {
        item = Zup.getInstance().getReportItemService().getReportItem(itemId);
        ReportHistoryItem[] history =
            Zup.getInstance().getReportItemService().getReportItemHistory(itemId);
        List<SyncAction> actionIterator =
            Zup.getInstance().getSyncActionService().getUnsuccesfullSyncActions();
        int size = actionIterator.size();
        for (int index = 0; index < size; index++) {
          SyncAction action = actionIterator.get(index);
          if (action instanceof EditReportItemSyncAction) {
            EditReportItemSyncAction editAction = (EditReportItemSyncAction) action;
            if (editAction.reportId == itemId) {
              item.address = editAction.address;
              item.description = editAction.description;
              item.category_id = editAction.categoryId;
            }
          }
        }
        bundle.putParcelable("item", item);
        bundle.putParcelable("user", item.user);
        if (history != null) {
          bundle.putParcelableArray("history", history);
        }
        if (item.relatedEntities != null
            && item.relatedEntities.cases != null
            && item.relatedEntities.cases.length > 0) {
          bundle.putParcelableArray("cases", item.relatedEntities.cases);
        }
        itemLoaded();
      } else {
        loadItem(itemId);
      }
    }

    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.registerReceiver(createdReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        int itemId = intent.getIntExtra("report_id", -1);
        ReportItem.Comment comment =
            (ReportItem.Comment) intent.getExtras().getParcelable("comment");
        commentCreated(itemId, comment);
      }
    }, new IntentFilter(PublishReportCommentSyncAction.REPORT_COMMENT_CREATED));
    manager.registerReceiver(editedReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        updateDetailsSyncReport((ReportItem) intent.getParcelableExtra("report"));
      }
    }, new IntentFilter(EditReportItemSyncAction.REPORT_EDITED));
    manager.registerReceiver(changedResponsableUserReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        ReportItem item = intent.getExtras().getParcelable("report");
        if (bundle == null) {
          bundle = new Bundle();
        }
        bundle.putParcelable("item", item);
        <<<<<<<HEAD =======
        if (item.relatedEntities != null
            && item.relatedEntities.cases != null
            && item.relatedEntities.cases.length > 0) {
          bundle.putParcelableArray("cases", item.relatedEntities.cases);
        }
        >>>>>>>unicef - fix refreshData();
      }
    }, new IntentFilter(ChangeReportResponsableUserSyncAction.REPORT_RESPONSABLE_USER_ASSIGNED));
    manager.registerReceiver(changedResponsableGroupReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        ReportItem item = intent.getExtras().getParcelable("report");
        if (bundle == null) {
          bundle = new Bundle();
        }
        bundle.putParcelable("item", item);
        <<<<<<<HEAD =======
        if (item.relatedEntities != null
            && item.relatedEntities.cases != null
            && item.relatedEntities.cases.length > 0) {
          bundle.putParcelableArray("cases", item.relatedEntities.cases);
        }
        >>>>>>>unicef - fix refreshData();
      }
    }, new IntentFilter(ChangeReportResponsableGroupSyncAction.REPORT_RESPONSABLE_GROUP_ASSIGNED));
  }

  private void updateDetailsSyncReport(ReportItem item) {
    if (bundle == null) {
      bundle = new Bundle();
    }
    bundle.putParcelable("item", item);
    bundle.putParcelable("user", item.user);
    <<<<<<<HEAD =======
    if (item.relatedEntities != null
        && item.relatedEntities.cases != null
        && item.relatedEntities.cases.length > 0) {
      bundle.putParcelableArray("cases", item.relatedEntities.cases);
    }
    >>>>>>>unicef - fix itemLoaded();
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.unregisterReceiver(editedReceiver);
  }

  private void loadNotifications(int itemId, int categoryId) {
    Zup.getInstance()
        .getService()
        .retrieveReportNotificationCollection(itemId, categoryId,
            new Callback<ReportNotificationCollection>() {
              @Override public void success(ReportNotificationCollection reportNotification,
                  Response response) {
                if (reportNotification != null && reportNotification.notifications != null) {
                  if (reportNotification.notifications != null
                      && reportNotification.notifications.length > 0) {
                    ArrayList<ReportNotificationCollection.ReportNotificationItem>
                        notificationItems = new ArrayList<>();
                    for (int index = 0; index < reportNotification.notifications.length; index++) {
                      if (reportNotification.notifications[index].deadlineInDays != 0) {
                        notificationItems.add(reportNotification.notifications[index]);
                      }
                      <<<<<<<HEAD
                    } if (notificationItems.size() > 0) {
                      ReportNotificationCollection.ReportNotificationItem[] notificationsToShow =
                          new ReportNotificationCollection.ReportNotificationItem[notificationItems.size()];
                      notificationsToShow = notificationItems.toArray(notificationsToShow);
                      bundle.putParcelableArray("notifications", notificationsToShow);
                    }
                    =======
                  } if (notificationItems.size() > 0) {
                    ReportNotificationCollection.ReportNotificationItem[] notificationsToShow =
                        new ReportNotificationCollection.ReportNotificationItem[notificationItems.size()];
                    notificationsToShow = notificationItems.toArray(notificationsToShow);
                    bundle.putParcelableArray("notifications", notificationsToShow);
                  }
                  >>>>>>>unicef - fix
                }
              }

              itemLoaded();
            }

    @Override public void failure (RetrofitError error){
      Log.e("RETROFIT", "Could not load report notifications", error);
      itemLoaded();
    }
  }

  );
}

  @Override protected void onDestroy() {
    super.onDestroy();
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.unregisterReceiver(createdReceiver);
    manager.unregisterReceiver(editedReceiver);
    manager.unregisterReceiver(changedResponsableGroupReceiver);
    manager.unregisterReceiver(changedResponsableUserReceiver);
  }

  private void commentCreated(int itemId, ReportItem.Comment comment) {
    if (itemId == getItem().id) {
      // Add the comment to the loaded item
      if (!Zup.getInstance().getReportItemService().hasReportItem(itemId)) {
        ReportItem item = getItem();
        item.addComment(comment);

        bundle.putParcelable("item", item);
      } else {
        ReportItem item = Zup.getInstance().getReportItemService().getReportItem(itemId);
        item.addComment(comment);
        if (item != null) {
          // Refresh the item in the bundle
          bundle.putParcelable("item", item);
        }
        Zup.getInstance().getReportItemService().addReportItem(item);
      }
      comments.refresh(getItem());
      internalComments.refresh(getItem());
      history.refresh();
    }
  }

  private void commentCreationFailed(int itemId, int type, String message) {
    ZupApplication.toast(findViewById(android.R.id.content), R.string.unable_create_comment).show();
  }

  ReportItem getItem() {
    return (ReportItem) bundle.getParcelable("item");
  }

  @Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
    super.onSaveInstanceState(outState, outPersistentState);

    outState.putAll(bundle);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putAll(bundle);
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    this.bundle = savedInstanceState;
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
    super.onRestoreInstanceState(savedInstanceState, persistentState);
    this.bundle = savedInstanceState;
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.report_item_details, menu);
    this.menu = menu;
    setUpMenu();
    return super.onCreateOptionsMenu(menu);
  }

  private void setUpMenu() {
    if (this.menu == null || this.getItem() == null) {
      return;
    }
    if (Zup.getInstance().getReportItemService().hasReportItem(getItem().id)) {
      menu.findItem(R.id.action_delete_local).setVisible(true);
      menu.findItem(R.id.action_download).setVisible(false);
    } else {
      menu.findItem(R.id.action_delete_local).setVisible(false);
      menu.findItem(R.id.action_download).setVisible(true);
    }

    int categoryId = getItem().category_id;
    menu.findItem(R.id.action_edit)
        .setVisible(Zup.getInstance().getAccess().canEditReportItem(categoryId) || Zup.getInstance()
            .getAccess()
            .canAlterReportItemStatus(categoryId));

    menu.findItem(R.id.action_delete)
        .setVisible(Zup.getInstance().getAccess().canDeleteReportItem(categoryId));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (this.getItem() == null) {
      return false;
    }
    int id = item.getItemId();
    switch (id) {
      case R.id.action_delete:
        showConfirmDeleteDialog();
        break;
      case R.id.action_edit:
        showEditScreen();
        break;
      case R.id.action_download:
        saveItem();
        setUpMenu();
        break;
      case R.id.action_delete_local:
        Zup.getInstance().getReportItemService().deleteReportItem(getItem().id);
        setUpMenu();
        setResult(RESULT_CHANGED);
        break;
      default:
        return false;
    }
    return super.onOptionsItemSelected(item);
  }

  void showConfirmDeleteDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.usure));
    builder.setMessage(getString(R.string.delete_report_confirm_text));
    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int which) {
        confirmDelete();
      }
    });
    builder.setNegativeButton(R.string.cancel, null);
    builder.show();
  }

  void confirmDelete() {
    DeleteReportItemSyncAction action = new DeleteReportItemSyncAction(getItem().id);
    Zup.getInstance().getSyncActionService().addSyncAction(action);

    Zup.getInstance().sync();
    Zup.getInstance().getReportItemService().deleteReportItem(getItem().id);
    setResult(RESULT_DELETED);
    finish();
  }

  void showEditScreen() {
    Intent intent = new Intent(this, CreateReportItemActivity.class);
    if (action != null) {
      intent.putExtra("action", action);
    } else {
      intent.putExtra("item", this.getItem());
    }
    startActivityForResult(intent, REQUEST_EDIT_REPORT);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_EDIT_REPORT
        && resultCode == CreateReportItemActivity.RESULT_REPORT_CHANGED) {
      if (Utilities.isConnected(this)) {
        showLoading();
        return;
      }

      ReportItem item = data.getExtras().getParcelable("item");
      this.bundle.putParcelable("item", item);
      itemLoaded();
    }
  }

  private void refreshData() {
    generalInfo.refresh();
    images.refresh();
    map.refresh();
    history.refresh();
    extraInfo.refresh();
  }

  void loadItem(int id) {
    if (id == -1) {
      finish();
      return;
    }

    Zup.getInstance().getService().retrieveReportItem(id, this);
    showLoading();
  }

  void saveItem() {
    if (isFinishing()) {
      return;
    }
    dialog = ViewUtils.createProgressDialog(this);
    dialog.show();

    ReportItemDownloader downloader =
        new ReportItemDownloader(getApplicationContext(), getItem().id,
            new ReportItemDownloader.Listener() {
              @Override public void onProgress(float progress) {
                if (isFinishing()) {
                  return;
                }
                dialog.setProgress((int) (progress * 100.0f));
              }

              @Override public void onFinished() {
                if (isFinishing()) {
                  return;
                }
                if (dialog.isShowing()) {
                  dialog.dismiss();
                }
                setUpMenu();
              }

              @Override public void onError() {
                showDownloadError();
              }
            });
    downloader.execute();
  }

  void showDownloadError() {
    ZupApplication.toast(findViewById(android.R.id.content), R.string.error_unable_load_report_item)
        .show();
  }

  void showLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.VISIBLE);
    findViewById(R.id.report_loading).setVisibility(View.VISIBLE);
  }

  void hideLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.GONE);
    findViewById(R.id.report_loading).setVisibility(View.GONE);
  }

  void itemLoaded() {
    hideLoading();
    LinearLayout layoutList = ((LinearLayout) findViewById(R.id.listView));
    if (layoutList == null) {
      return;
    }
    layoutList.removeAllViews();

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    if (bundle.containsKey("cases")) {
      relatedCases = new ReportItemCasesFragment();
      relatedCases.setArguments(this.bundle);
      transaction.add(R.id.listView, relatedCases, "related_cases");
    }

    generalInfo = new ReportItemGeneralInfoFragment();
    generalInfo.setArguments(this.bundle);
    transaction.add(R.id.listView, generalInfo, "general");

    images = new ReportItemImagesFragment();
    images.setArguments(this.bundle);
    transaction.add(R.id.listView, images, "images");

    extraInfo = new ReportItemExtraInfoFragment();
    extraInfo.setArguments(this.bundle);
    transaction.add(R.id.listView, extraInfo, "extra_info");

    map = new ReportItemMapFragment();
    map.setArguments(this.bundle);
    transaction.add(R.id.listView, map, "map");

    userInfo = new ReportItemUserInfoFragment();
    userInfo.setArguments(this.bundle);
    transaction.add(R.id.listView, userInfo, "user_info");

    if (bundle.containsKey("notifications")) {
      notificationInfo = new ReportItemNotificationFragment();
      notificationInfo.setArguments(this.bundle);
      transaction.add(R.id.listView, notificationInfo, "notification_info");
    }

    comments = new ReportItemCommentsFragment();
    Bundle commentsBundle = new Bundle(this.bundle);
    commentsBundle.putInt("filter_type", ReportItemCommentsAdapter.FILTER_COMMENTS);
    comments.setArguments(commentsBundle);
    transaction.add(R.id.listView, comments, "comments");

    internalComments = new ReportItemCommentsFragment();
    commentsBundle = new Bundle(this.bundle);
    commentsBundle.putInt("filter_type", ReportItemCommentsAdapter.FILTER_INTERNAL);
    internalComments.setArguments(commentsBundle);
    transaction.add(R.id.listView, internalComments, "comments_internal");

    history = new ReportItemHistoryFragment();
    history.setArguments(this.bundle);
    transaction.add(R.id.listView, history, "history");

    ReportItemHistoryFragment history2 = new ReportItemHistoryFragment();
    history2.setArguments(this.bundle);
    transaction.add(R.id.listView, history2, "history2");

    transaction.hide(history2);

    if (getItem().images == null || getItem().images.length == 0) transaction.hide(images);

    try {
      transaction.commit();
    } catch (Exception ex) {
      // FIXME Sometimes this will hang and crash after the activity is finished
      ex.printStackTrace();
    }
    setUpMenu();
  }

  @Override
  public void success(SingleReportItemCollection singleReportItemCollection, Response response) {
    ReportItem item = singleReportItemCollection.report;
    loadNotifications(item.id, item.category_id);
    bundle.putParcelable("item", item);
    bundle.putParcelable("user", item.user);
    if (item.relatedEntities != null
        && item.relatedEntities.cases != null
        && item.relatedEntities.cases.length > 0) {
      bundle.putParcelableArray("cases", item.relatedEntities.cases);
    }
    if (Zup.getInstance().getReportItemService().hasReportItem(item.id)) {
      saveItem();
    }
  }

  @Override public void failure(RetrofitError error) {
    Log.e("RETROFIT", "Could not load report item", error);

    ZupApplication.toast(findViewById(android.R.id.content), R.string.error_loading_report_item)
        .show();
    finish();
  }
}

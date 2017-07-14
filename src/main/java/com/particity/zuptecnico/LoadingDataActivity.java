package com.ntxdev.zuptecnico;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ntxdev.zuptecnico.activities.RootActivity;
import com.ntxdev.zuptecnico.activities.cases.CasesListActivity;
import com.ntxdev.zuptecnico.activities.inventory.InventoryListActivity;
import com.ntxdev.zuptecnico.activities.reports.ReportsListActivity;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.sync.SyncAction;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.Group;
import com.ntxdev.zuptecnico.entities.collections.FlowCollection;
import com.ntxdev.zuptecnico.entities.collections.GroupCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.ReportCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;
import com.ntxdev.zuptecnico.entities.responses.NamespaceCollection;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.Iterator;

import retrofit.RetrofitError;

public class LoadingDataActivity extends AppCompatActivity {
  private Tasker tasker;
  private boolean syncNow = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_loading);

    Zup.getInstance().initStorage(this);

    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;
      ((TextView) findViewById(R.id.version)).setText(version);
    } catch (Exception e) {
      e.printStackTrace();
    }

    begin();
  }

  void begin() {
    tasker = new Tasker();
    tasker.execute();
  }

  void everythingLoaded() {
    Zup.getInstance().setHasFullLoad(this);
    if (Utilities.isConnected(this) && Zup.getInstance()
        .getSyncActionService()
        .hasPendingSyncActions()) {
      showPendingSyncDialog();
      return;
    } else {
      goToNextPage();
    }
  }

  void goToNextPage() {
    Intent intent;
    if (Zup.getInstance().getAccess().canViewReportItems() && BuildConfig.REPORT_ENABLED) {
      intent = new Intent(this, ReportsListActivity.class);
    } else if (Zup.getInstance().getAccess().canViewIventoryItems()
        && BuildConfig.INVENTORY_ENABLED) {
      intent = new Intent(this, InventoryListActivity.class);
    } else if (BuildConfig.CASE_ENABLED) {
      intent = new Intent(this, CasesListActivity.class);
    } else {
      intent = new Intent(this, ProfileActivity.class);
    }
    intent.putExtra(RootActivity.SYNC_NOW, syncNow);
    startActivity(intent);
    finish();
  }

  private void showPendingSyncDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.attention));
    builder.setMessage(getString(R.string.sync_now_alert_message));
    builder.setPositiveButton(getString(R.string.sync_now), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        syncNow = true;
        goToNextPage();
      }
    });
    builder.setNegativeButton(getString(R.string.sync_later),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            goToNextPage();
            dialog.dismiss();
          }
        });
    builder.create().show();
  }

  @Override public void onBackPressed() {

  }

  public void onJobFailed() {
    if (isFinishing()) {
      return;
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false);

    if (Zup.getInstance().hasFullLoad(this)) {
      builder.setMessage(getString(R.string.error_session_outdated));
      builder.setPositiveButton(getString(R.string.continue_text),
          new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
              everythingLoaded();
            }
          });
    } else {
      builder.setMessage(getString(R.string.error_no_internet_loading));
      builder.setPositiveButton(getString(R.string.try_again),
          new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
              begin();
            }
          });
      builder.setNegativeButton(R.string.logout_text, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialog, int which) {
          logout();
        }
      });
    }
    try {
      builder.show();
    } catch (WindowManager.BadTokenException e) {
      //If this happens, the app already left this activity and the dialog should not be displayed. Ignore the exception
    }
  }

  private void logout() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false);
    if (Zup.getInstance().getSyncActionService().hasPendingSyncActions()) {
      builder.setTitle(R.string.error_title);
      builder.setMessage(getString(R.string.error_pending_sync_logout));
      builder.setPositiveButton(R.string.lab_ok, null);
      builder.show();
    } else {
      Zup.getInstance().clearStorage(this);

      Intent intent = new Intent(this, LoginActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      this.startActivity(intent);
    }
  }

  class Tasker extends AsyncTask<Void, String, Boolean> {
    @Override protected Boolean doInBackground(Void... voids) {
      try {
        this.loadUser();
        if (BuildConfig.INVENTORY_ENABLED) {
          this.loadCategories();
        }
        if (BuildConfig.REPORT_ENABLED) {
          this.loadReportCategories();
        }
        loadNamespaces();
        loadGroups();
        //this.loadFlows();

        return true;
      } catch (Exception ex) {
        Crashlytics.logException(ex);
        if (ex instanceof InvalidTokenException) {
          Intent intent = new Intent(LoadingDataActivity.this, LoginActivity.class);
          intent.putExtra(LoginActivity.EXPIRED_TOKEN, true);
          startActivity(intent);
          finish();
          return false;
        }
        Log.e("Loading data", ex.getMessage(), ex);
        return false;
      }
    }

    @Override protected void onProgressUpdate(String... values) {
      super.onProgressUpdate(values);

      TextView tv = (TextView) findViewById(R.id.loading_status);
      tv.setText(values[0]);
    }

    void loadGroups() {
      try {
        this.publishProgress(getString(R.string.loading_groups));
          GroupCollection groupCollection = Zup.getInstance().getService().retrieveGroups();
          Zup.getInstance().getGroupService().clear();
          Zup.getInstance().getGroupService().addGroups(groupCollection.groups);

        groupCollection = Zup.getInstance().getService().retrieveNamespaceGroups();
        Zup.getInstance().getGroupService().addNamespaceGroups(groupCollection.groups);
      } catch (RetrofitError error) {
        if (error.getResponse() == null || error.getResponse().getStatus() != 403) {
          Crashlytics.logException(error);
          throw error;
        }
      }
    }

    void loadNamespaces() {
      try {
        NamespaceCollection namespaceCollection = Zup.getInstance().getService().getNamespaces();
        if (namespaceCollection != null) {
          Zup.getInstance().getNamespaceService().setNamespaces(namespaceCollection.namespaces);
        }
      } catch (RetrofitError error) {
        if (error.getResponse() == null || error.getResponse().getStatus() != 403) {
          Crashlytics.logException(error);
          throw error;
        }
      }
    }

    void loadCategories() {
      try {
        this.publishProgress(getString(R.string.loading_inventory_categories));
        InventoryCategoryCollection categories =
            Zup.getInstance().getService().getInventoryCategories();
        Zup.getInstance()
            .getInventoryCategoryService()
            .setInventoryCategories(categories.categories);
      } catch (RetrofitError error) {
        if (error.getResponse() == null || error.getResponse().getStatus() != 403) {
          Crashlytics.logException(error);
          throw error;
        }
      }
    }

    void loadReportCategories() {
      try {
        this.publishProgress(getString(R.string.loading_report_categories));
        ReportCategoryCollection categories = Zup.getInstance().getService().getReportCategories();
        Zup.getInstance().getReportCategoryService().setReportCategories(categories.categories);
      } catch (RetrofitError error) {
        if (error.getResponse() == null || error.getResponse().getStatus() != 403) {
          Crashlytics.logException(error);
          throw error;
        }
      }
    }

    void loadFlows() {
      try {
        publishProgress(getString(R.string.loading_flows));
        Flow[] flowCollection = Zup.getInstance().getService().retrieveFlows().flows;
        if (flowCollection == null) {
          return;
        }
        for (int index = 0; index < flowCollection.length; index++) {
          Flow flow = flowCollection[index];
          Zup.getInstance().getFlowService().addFlow(flow);
        }
      } catch (RetrofitError error) {
        if (error.getResponse() == null || error.getResponse().getStatus() != 403) {
          Crashlytics.logException(error);
          throw error;
        }
      }
    }

    void loadUser() throws InvalidTokenException {
      try {
        this.publishProgress(getString(R.string.loading_user_details));

        SingleUserCollection userCollection =
            Zup.getInstance().getService().retrieveUser(Zup.getInstance().getSessionUserId());
        Zup.getInstance().getUserService().addUser(userCollection.user);
        Zup.getInstance().refreshAccess();
      } catch (RetrofitError error) {
        boolean isInvalidToken;
        try {
          isInvalidToken = error.getResponse() != null && error.getResponse().getStatus() == 401;
        } catch (Exception e) {
          isInvalidToken = false;
        }
        if (isInvalidToken) {
          throw new InvalidTokenException();
        } else {
          Crashlytics.logException(error);
        }
      }
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
      super.onPostExecute(aBoolean);
      if (isFinishing()) {
        return;
      }
      if (aBoolean) {
        everythingLoaded();
      } else {
        onJobFailed();
      }
    }
  }

  class InvalidTokenException extends Exception {

  }
}

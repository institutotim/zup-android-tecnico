package com.ntxdev.zuptecnico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ntxdev.zuptecnico.BuildConfig;
import com.ntxdev.zuptecnico.ProfileActivity;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.SyncActivity;
import com.ntxdev.zuptecnico.activities.cases.CasesListActivity;
import com.ntxdev.zuptecnico.activities.inventory.InventoryListActivity;
import com.ntxdev.zuptecnico.activities.reports.ReportsListActivity;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.ui.UIHelper;

public abstract class RootActivity extends AppCompatActivity
    implements UIHelper.UpdateDrawerStatus {
  public static final String SYNC_NOW = "sync_now";
  private DrawerLayout mDrawerLayout;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UIHelper.initActivity(this);
    if (getIntent() != null
        && getIntent().getBooleanExtra(SYNC_NOW, false)
        && !(this instanceof SyncActivity)) {
      Intent intent = new Intent(this, SyncActivity.class);
      intent.putExtra(SYNC_NOW, true);
      startActivity(intent);
    }
  }

  @Override protected void onResume() {
    initSidebar();
    super.onResume();
  }

  @Override public void onBackPressed() {
    if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  public void initSidebar() {
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (mDrawerLayout != null) {
      ViewGroup sidebar = (ViewGroup) findViewById(R.id.sidebar_root);
      initSidebarHeader(sidebar);
      View cellProfile = sidebar.findViewById(R.id.sidebar_cell_profile);
      View cellReports = sidebar.findViewById(R.id.sidebar_cell_reports);
      View cellDocuments = sidebar.findViewById(R.id.sidebar_cell_documents);
      View cellItems = sidebar.findViewById(R.id.sidebar_cell_items);
      View cellNotifications = sidebar.findViewById(R.id.sidebar_cell_notifications);
      View cellSync = sidebar.findViewById(R.id.sidebar_cell_sync);
      updateDrawerStatus();
      updateDrawerByPermissions(cellReports, cellItems, cellDocuments);

      updateSidebarItemClick(cellProfile, cellReports, cellDocuments, cellItems, cellSync);
    }
  }

  private void updateDrawerByPermissions(View reports, View items, View cases) {
    if (!Zup.getInstance().getAccess().canViewReportItems() || !BuildConfig.REPORT_ENABLED) {
      reports.setVisibility(View.GONE);
    } else {
      reports.setVisibility(View.VISIBLE);
    }
    if (!Zup.getInstance().getAccess().canViewIventoryItems() || !BuildConfig.INVENTORY_ENABLED) {
      items.setVisibility(View.GONE);
    } else {
      items.setVisibility(View.VISIBLE);
    }

    if (!Zup.getInstance().getAccess().canViewCases() || !BuildConfig.CASE_ENABLED) {
      cases.setVisibility(View.GONE);
    } else {
      cases.setVisibility(View.VISIBLE);
    }
  }

  private void updateSidebarItemClick(View cellProfile, View cellReports, View cellDocuments,
      View cellItems, View cellSync) {
    View.OnClickListener onClickListener = new View.OnClickListener() {
      @Override public void onClick(View v) {
        Class<?> activity = (Class<?>) v.getTag();
        if (activity.equals(RootActivity.this.getClass())) {
          toggleSidebar();
        } else {
          startActivity(new Intent(RootActivity.this, activity));
        }
      }
    };
    cellReports.setTag(ReportsListActivity.class);
    cellReports.setOnClickListener(onClickListener);
    cellProfile.setTag(ProfileActivity.class);
    cellProfile.setOnClickListener(onClickListener);
    cellDocuments.setTag(CasesListActivity.class);
    cellDocuments.setOnClickListener(onClickListener);
    cellItems.setTag(InventoryListActivity.class);
    cellItems.setOnClickListener(onClickListener);
    cellSync.setTag(SyncActivity.class);
    cellSync.setOnClickListener(onClickListener);
  }

  private void initSidebarHeader(ViewGroup sidebar) {
    TextView txtName = (TextView) sidebar.findViewById(R.id.sidebar_label_name);
    TextView txtEmail = (TextView) sidebar.findViewById(R.id.sidebar_label_group);
    User user = Zup.getInstance().getSessionUser();
    if (user != null) {
      txtName.setText(user.name);
      txtEmail.setText(user.email);
    }
  }

  public void toggleSidebar() {
    DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (mDrawerLayout == null) {
      return;
    }
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      mDrawerLayout.openDrawer(GravityCompat.START);
    }
  }

  @Override protected void onDestroy() {
    DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (mDrawerLayout != null) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    }
    super.onDestroy();
  }
}

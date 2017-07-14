package com.ntxdev.zuptecnico.activities.cases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.adapters.CaseStepsAdapter;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.sync.DeleteCaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.FillCaseStepSyncAction;
import com.ntxdev.zuptecnico.api.sync.FinishCaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.SyncAction;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.collections.SingleCaseCollection;
import com.ntxdev.zuptecnico.fragments.cases.CaseItemStepsListFragment;
import com.ntxdev.zuptecnico.fragments.cases.RelatedReportsSectionFragment;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.Utilities;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CaseItemDetailsActivity extends AppCompatActivity
    implements Callback<SingleCaseCollection>, CaseStepsAdapter.OnCaseStepClickListener {
  public static final String CASE_ID = "case_id";
  public static final String CASE = "case";
  public static final String FLOW_ID = "flowId";
  public static final String FLOW_VERSION = "flowVersion";
  public static final String CASE_STEPS = "steps";
  private static final int RESULT_DELETED = 4;
  CaseItemStepsListFragment stepsList;
  RelatedReportsSectionFragment relatedReportsSectionFragment;
  Case flowCase;
  Bundle bundle;
  Menu menu;
  private BroadcastReceiver caseFinishedReceiver;
  private BroadcastReceiver updatedReceiver;
  private BroadcastReceiver endedSyncReceiver;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_case_item_details);
    UIHelper.initActivity(this);

    if (savedInstanceState != null) {
      bundle = savedInstanceState;
      hideLoading();
      if (getItem() != null) {
        itemLoaded();
      }
    } else {
      bundle = new Bundle();
      int caseId = getIntent().getIntExtra(CASE_ID, -1);
      if (Zup.getInstance().getCaseItemService().hasCaseItem(caseId)) {
        flowCase = Zup.getInstance().getCaseItemService().getCaseItem(caseId);
        if (flowCase != null) {
          bundle.putParcelableArray(CASE_STEPS, flowCase.getSteps());
        }
        bundle.putParcelable(CASE, flowCase);
        itemLoaded();
      } else {
        loadItem(caseId);
      }
    }
    final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
    manager.registerReceiver(caseFinishedReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        bundle = new Bundle();
        flowCase = intent.getParcelableExtra("case");
        if (flowCase != null) {
          bundle.putParcelableArray(CASE_STEPS, flowCase.getSteps());
          bundle.putParcelable(CASE, flowCase);
          itemLoaded();
        } else if (intent.hasExtra("caseId")) {
          loadItem(intent.getIntExtra("caseId", -1));
        }
        manager.unregisterReceiver(endedSyncReceiver);
      }
    }, new IntentFilter(FinishCaseSyncAction.CASE_FINISHED));

    manager.registerReceiver(updatedReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        boolean mustFinish = bundle != null && bundle.containsKey("must_finish");
        bundle = new Bundle();
        flowCase = intent.getParcelableExtra("case");
        if (flowCase != null) {
          if (Zup.getInstance().getCaseItemService().hasCaseItem(flowCase.id)) {
            Zup.getInstance().getCaseItemService().addCaseItem(flowCase);
          }
          bundle.putParcelableArray(CASE_STEPS, flowCase.getSteps());
          bundle.putParcelable(CASE, flowCase);
          itemLoaded();
          if (mustFinish) {
            finishCase();
          }
        }
      }
    }, new IntentFilter(FillCaseStepSyncAction.FILL_STEP));

    manager.registerReceiver(endedSyncReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        if (bundle == null || !bundle.containsKey("case")) {
          Toast.makeText(CaseItemDetailsActivity.this, R.string.error_loading_item, Toast.LENGTH_SHORT).show();
          finish();
          return;
        }
        itemLoaded();
      }
    }, new IntentFilter(SyncAction.ACTION_SYNC_END));
  }

  @Override protected void onDestroy() {
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
    manager.unregisterReceiver(updatedReceiver);
    manager.unregisterReceiver(caseFinishedReceiver);
    manager.unregisterReceiver(endedSyncReceiver);
    super.onDestroy();
  }

  Case getItem() {
    return (Case) bundle.getParcelable(CASE);
  }

  @Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
    super.onSaveInstanceState(outState, outPersistentState);
    outState.putAll(bundle);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putAll(bundle);
  }

  void finishCase() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.select_resolution_state_title));
    final Case item = getItem();
    if (item.initialFlow == null || item.initialFlow.resolution_states == null) {
      return;
    }
    final Flow.ResolutionState[] states = item.initialFlow.resolution_states;
    String[] defaultItems = new String[states.length];
    for (int index = 0; index < states.length; index++) {
      defaultItems[index] = states[index].title;
    }

    builder.setItems(defaultItems, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        Flow.ResolutionState state = states[i];
        FinishCaseSyncAction action = new FinishCaseSyncAction(item.id, state.id);
        Zup.getInstance().getSyncActionService().addSyncAction(action);
        Zup.getInstance().sync();
        if (!Utilities.isConnected(CaseItemDetailsActivity.this)) {
          finish();
        }
      }
    });
    builder.create().show();
  }

  public void finishCase(View v) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.usure));
    builder.setMessage(getString(R.string.finish_case_confirm_text));
    builder.setPositiveButton(R.string.finish_case_title, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int which) {
        finishCase();
      }
    });
    builder.setNegativeButton(R.string.cancel, null);
    builder.show();
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
    getMenuInflater().inflate(R.menu.case_steps_list, menu);
    this.menu = menu;
    setUpMenu();
    return super.onCreateOptionsMenu(menu);
  }

  private void setUpMenu() {
    if (this.menu != null && flowCase != null) {
      if (Zup.getInstance().getCaseItemService().hasCaseItem(flowCase.id)) {
        menu.findItem(R.id.action_delete_local).setVisible(true);
        menu.findItem(R.id.action_download).setVisible(false);
      } else {
        menu.findItem(R.id.action_delete_local).setVisible(false);
        menu.findItem(R.id.action_download).setVisible(true);
      }
      menu.findItem(R.id.action_delete)
          .setVisible(Zup.getInstance().getAccess().canDeleteCase(flowCase.id));
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (flowCase == null || isLoading()) {
      return false;
    }
    switch (item.getItemId()) {
      case R.id.action_delete_local:
        Zup.getInstance().getCaseItemService().deleteCaseItem(flowCase.id);
        setUpMenu();
        break;
      case R.id.action_delete:
        showConfirmDeleteDialog();
        break;
      case R.id.action_download:
        Zup.getInstance().getCaseItemService().addCaseItem(flowCase);
        setUpMenu();
        break;
      case R.id.action_history:
        Intent intent = new Intent(this, CaseHistoryActivity.class);
        intent.putExtra("case_id", flowCase.id);
        startActivity(intent);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  void showConfirmDeleteDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.usure));
    builder.setMessage(getString(R.string.delete_case_confirm_text));
    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int which) {
        confirmDelete();
      }
    });
    builder.setNegativeButton(R.string.cancel, null);
    builder.show();
  }

  void confirmDelete() {
    DeleteCaseSyncAction action = new DeleteCaseSyncAction(getItem().id);
    Zup.getInstance().getSyncActionService().addSyncAction(action);

    Zup.getInstance().sync();
    setResult(RESULT_DELETED);
    finish();
  }

  void loadItem(int id) {
    if (id == -1) {
      finish();
      return;
    }

    Zup.getInstance().getService().retrieveCase(id, this);
    showLoading();
  }

  boolean isLoading() {
    return findViewById(R.id.wait_sync_standard_message).getVisibility() == View.VISIBLE;
  }

  void showLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.VISIBLE);
    if (Zup.getInstance().isSyncing()) {
        ((TextView) findViewById(R.id.wait_sync_standard_message)).setText("Aguarde um momento, os dados preenchidos estão sendo sincronizados...");
    } else {
        ((TextView) findViewById(R.id.wait_sync_standard_message)).setText("Carregando item...");
    }
    findViewById(R.id.report_loading).setVisibility(View.VISIBLE);
    findViewById(R.id.container).setVisibility(View.INVISIBLE);
  }

  void hideLoading() {
    findViewById(R.id.wait_sync_standard_message).setVisibility(View.GONE);
    findViewById(R.id.report_loading).setVisibility(View.GONE);
    findViewById(R.id.container).setVisibility(View.VISIBLE);
  }

  void itemLoaded() {
    if (isFinishing()) {
      return;
    }
    if (Zup.getInstance().isSyncing()) {
      showLoading();
      return;
    }
    loadHeader();
    findViewById(R.id.finish_case_button).setVisibility(
        getItem().getStatus().equals("finished") ? View.GONE : View.VISIBLE);
    ((ViewGroup) findViewById(R.id.listView)).removeAllViews();
    hideLoading();
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (stepsList != null) {
      transaction.remove(stepsList);
    }

    if (relatedReportsSectionFragment != null) {
      transaction.remove(relatedReportsSectionFragment);
    }

    if (flowCase.relatedItems != null) {
      findViewById(R.id.related_reports).setVisibility(View.VISIBLE);
      relatedReportsSectionFragment = new RelatedReportsSectionFragment();
      relatedReportsSectionFragment.setArguments(this.bundle);
      transaction.replace(R.id.related_reports, relatedReportsSectionFragment);
    } else {
      findViewById(R.id.related_reports).setVisibility(View.GONE);
    }

    stepsList = new CaseItemStepsListFragment();
    stepsList.setArguments(this.bundle);
    transaction.add(R.id.listView, stepsList, CASE_STEPS);
    try {
      transaction.commit();
    } catch (Exception ex) {
      Log.e("Error", "Transaction error", ex);
    }
  }

  private void loadHeader() {
    findViewById(R.id.case_container).setBackgroundColor(
        ContextCompat.getColor(this, android.R.color.transparent));
    TextView title = (TextView) findViewById(R.id.case_title);
    TextView flowType = (TextView) findViewById(R.id.flow_title);
    TextView createdDate = (TextView) findViewById(R.id.creation_date);
    TextView updatedDate = (TextView) findViewById(R.id.edition_date);
    TextView state = (TextView) findViewById(R.id.status_desc);
    ImageView stateicon = (ImageView) findViewById(R.id.status_icon);

    stateicon.setImageDrawable(ContextCompat.getDrawable(this,
        Zup.getInstance().getCaseStatusDrawable(flowCase.getStatus())));
    state.setTextColor(Zup.getInstance().getCaseStatusColor(this, flowCase.getStatus()));
    state.setText(Zup.getInstance().getCaseStatusString(this, flowCase.getStatus()));

    title.setText(getString(R.string.case_title) + " " + flowCase.id);
    if (flowCase.initialFlow != null) {
      flowType.setText(flowCase.initialFlow.title);
    }

    if (flowCase.createdAt != null) {
      createdDate.setText(getString(R.string.creation_date_title) + ": " + Zup.getInstance()
          .formatIsoDate(flowCase.createdAt));
    } else {
      createdDate.setVisibility(View.INVISIBLE);
    }

    if (flowCase.updatedAt != null) {
      updatedDate.setText(getString(R.string.edition_date_title) + " " + Zup.getInstance()
          .formatIsoDate(flowCase.updatedAt));
    } else {
      updatedDate.setVisibility(View.INVISIBLE);
    }
  }

  @Override public void success(SingleCaseCollection singleCaseCollection, Response response) {
    flowCase = singleCaseCollection.flowCase;
    bundle.putParcelable(CASE, flowCase);
    bundle.putParcelableArray(CASE_STEPS, flowCase.getSteps());
    itemLoaded();
  }

  @Override public void failure(RetrofitError error) {
    Log.e("RETROFIT", "Could not load case item", error);
    ZupApplication.toast(findViewById(android.R.id.content),
        getString(R.string.error_loading_report_item)).show();
    finish();
  }

  @Override public void onCaseStepClickListener(Case theCase, Flow.Step step) {
    if (isFinishing()) {
      return;
    }
    Intent intent;
    if (step.stepType.equals("flow")) {
      intent = new Intent(this, CaseItemDetailsActivity.class);
      intent.putExtra(CaseItemDetailsActivity.CASE_ID, getItem().id);
      intent.putExtra(CaseItemDetailsActivity.FLOW_ID, step.child_flow_id);
      intent.putExtra(CaseItemDetailsActivity.FLOW_VERSION, step.child_flow_version);
    } else {
      intent = new Intent(this, CaseItemFormDetailsActivity.class);
      intent.putExtra("case", theCase);
      intent.putExtra("stepId", step.id);
    }
    startActivityForResult(intent, 2);
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }

  @Override public void onBackPressed() {
    setResult(RESULT_OK);
    super.onBackPressed();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == 2) {
        bundle = data.getExtras();
        itemLoaded();
      }
    }
  }
}

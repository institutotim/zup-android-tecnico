package com.lfdb.zuptecnico.activities.cases;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.fragments.cases.CaseFormViewFragment;
import com.lfdb.zuptecnico.ui.UIHelper;

/**
 * Created by Renan on 11/12/2015.
 */
public class CaseItemFormDetailsActivity extends AppCompatActivity {
  Case theCase;
  Case.Step caseStep;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_report_item_details);
    Zup.getInstance().initStorage(getApplicationContext());
    UIHelper.initActivity(this);
    loadItem(getIntent());
  }

  private void loadItem(Intent intent) {
    showLoading();

    theCase = intent.getParcelableExtra("case");
    int stepId = intent.getIntExtra("stepId", -1);

    if (theCase == null || stepId == -1) {
      finish();
      return;
    }
    caseStep = theCase.getStep(stepId);
    if (caseStep == null) {
      finish();
      return;
    }
    fillItemInfo();

    if (Zup.getInstance().isSyncing()) {
      showLoading();
      return;
    }

    boolean ignoreLoading = intent.getBooleanExtra("ignore_loading", false);
    if (!ignoreLoading && theCase.isCurrentStep(caseStep.stepId)
        && caseStep.flowStep != null
        && theCase.initialFlow != null
        &&
        Zup.getInstance().getAccess().canEditStep(caseStep.flowStep.id, theCase.initialFlow.id)) {
      fillCaseStep();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.case_step_form, menu);
    menu.findItem(R.id.action_edit).setVisible(canEdit());
    return super.onCreateOptionsMenu(menu);
  }

  boolean canEdit() {
    if (caseStep == null || theCase == null || theCase.getStatus().equals("finished")) {
      return false;
    }
    if (theCase.isCurrentStep(caseStep.stepId)
        && caseStep.flowStep != null
        && theCase.initialFlow != null
        &&
        Zup.getInstance().getAccess().canEditStep(caseStep.flowStep.id, theCase.initialFlow.id)) {
      return true;
    }
    return false;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_edit:
        fillCaseStep();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void fillCaseStep() {
    Intent intent = new Intent(this, ViewCaseStepFormActivity.class);
    intent.putExtra("case", theCase);
    intent.putExtra("stepId", caseStep.stepId);
    startActivityForResult(intent, 2);
  }

  @Override public void onBackPressed() {
    Intent intent = new Intent();
    intent.putExtra(CaseItemDetailsActivity.CASE, theCase);
    intent.putExtra(CaseItemDetailsActivity.CASE_STEPS, theCase.getSteps());
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == 2 && Zup.getInstance().isSyncing()) {
        setResult(RESULT_OK, data);
        finish();
      } else if (requestCode == 2){
        loadItem(data);
      }
    }
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
    UIHelper.setTitle(this, caseStep.flowStep.title);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    ((ViewGroup) findViewById(R.id.listView)).removeAllViews();

    Bundle bundle = new Bundle();
    bundle.putParcelable("caseStep", caseStep);
    bundle.putParcelable("flowStep", caseStep.flowStep);
    bundle.putParcelable("case", theCase);
    CaseFormViewFragment fragment = new CaseFormViewFragment();
    fragment.setArguments(bundle);
    transaction.add(R.id.listView, fragment, "generalInfo");
    try {
      transaction.commit();
    } catch (Exception ex) {
      // FIXME Sometimes this will hang and crash after the activity is finished
    }
    hideLoading();
  }
}

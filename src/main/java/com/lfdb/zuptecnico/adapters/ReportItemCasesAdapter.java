package com.particity.zuptecnico.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.particity.zuptecnico.api.Zup;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.activities.cases.CaseItemDetailsActivity;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.util.Utilities;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Renan on 02/03/2016.
 */
public class ReportItemCasesAdapter extends BaseAdapter {
  Context mContext;
  SparseArray<View> mViewCache;
  Case[] mCases;

  public ReportItemCasesAdapter(Context context, Case[] cases) {
    mContext = context;
    mCases = cases;
    this.mViewCache = new SparseArray<>();
    Arrays.sort(mCases, new Comparator<Case>() {
      @Override public int compare(Case lhs, Case rhs) {
        return rhs.updatedAt.compareTo(lhs.updatedAt);
      }
    });
  }

  @Override public boolean isEnabled(int position) {
    return false;
  }

  @Override public int getCount() {
    return mCases.length;
  }

  @Override public Case getItem(int i) {
    return mCases[i];
  }

  @Override public long getItemId(int i) {
    return getItem(i).id;
  }

  @Override public View getView(int i, View v, ViewGroup viewGroup) {
    Case mCase = getItem(i);

    if (mViewCache.get(mCase.id) != null) {
      return mViewCache.get(mCase.id);
    } else {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      View view = inflater.inflate(R.layout.report_details_cases_item, viewGroup, false);
      fillData(view, mCase);
      boolean isLastItem = getCount() == i + 1;
      view.findViewById(R.id.list_divider).setVisibility(isLastItem ? View.GONE : View.VISIBLE);

      mViewCache.put(mCase.id, view);
      return view;
    }
  }

  void fillData(View root, final Case mCase) {
    if (mCase == null) return;

    TextView txtName = (TextView) root.findViewById(R.id.case_name);
    TextView txtCreatedAt = (TextView) root.findViewById(R.id.case_created_at);
    TextView txtUpdatedAt = (TextView) root.findViewById(R.id.case_updated_at);
    TextView txtStatus = (TextView) root.findViewById(R.id.case_status);
    TextView txtStepTitle = (TextView) root.findViewById(R.id.case_step_title);
    TextView txtStepResponsible = (TextView) root.findViewById(R.id.case_step_responsible);

    View.OnClickListener listener = new View.OnClickListener() {
      @Override public void onClick(View v) {
        openCaseItem(mCase.id);
      }
    };

    txtName.setOnClickListener(listener);
    root.findViewById(R.id.access_case).setOnClickListener(listener);

    txtName.setText(mCase.initialFlow.title + " #" + mCase.id);

    if (mCase.getCurrentStep() != null) {
      Case.Step currentStep = mCase.getCurrentStep();
      if (currentStep.responsableUser != null) {
        txtStepResponsible.setText(mContext.getString(R.string.case_conductor_report)
            + " "
            + currentStep.responsableUser.name);
        txtStepResponsible.setVisibility(View.VISIBLE);
      } else {
        txtStepResponsible.setVisibility(View.GONE);
      }
      if (currentStep.flowStep != null) {
        txtStepTitle.setText("Etapa atual do caso: " + currentStep.flowStep.title);
        txtStepTitle.setVisibility(View.VISIBLE);
      } else {
        txtStepTitle.setVisibility(View.GONE);
      }
    }

    String createdAt = mContext.getString(R.string.created_at) + " " +
        Utilities.formatIsoDateAndTime(mCase.createdAt);
    txtCreatedAt.setText(createdAt);

    String updatedAt = mContext.getString(R.string.updated_at) + " " +
        Utilities.formatIsoDateAndTime(mCase.updatedAt);
    txtUpdatedAt.setText(updatedAt);

    String status = mContext.getString(R.string.current_status) + " " +
        Zup.getInstance().getCaseStatusString(mContext, mCase.getStatus());
    txtStatus.setText(status);
  }

  public void openCaseItem(int id) {
    Intent intent = new Intent(mContext, CaseItemDetailsActivity.class);
    intent.putExtra(CaseItemDetailsActivity.CASE_ID, id);
    mContext.startActivity(intent);
    ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }
}

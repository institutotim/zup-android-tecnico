package com.lfdb.zuptecnico.fragments.cases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.activities.cases.CaseItemDetailsActivity;
import com.lfdb.zuptecnico.adapters.CaseRelatedReportsAdapter;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.ExpandExpandableListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by renan on 08/06/16.
 */
public class RelatedReportsSectionFragment extends Fragment {
  ExpandExpandableListView mExpandableListView;
  CaseRelatedReportsAdapter mAdapter;
  Case getItem() {
    return (Case) getArguments().getParcelable(CaseItemDetailsActivity.CASE);
  }


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
  Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_case_related_reports, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mExpandableListView = (ExpandExpandableListView) view.findViewById(R.id.related_reports);

    List<ReportItem> items = new ArrayList<>();
    Case mCase = getItem();
    if (mCase == null || mCase.relatedItems == null || mCase.relatedItems.reportItems == null || mCase.relatedItems.reportItems.length == 0) {
      view.setVisibility(View.GONE);
      return;
    }
    Collections.addAll(items, mCase.relatedItems.reportItems);

    mAdapter = new CaseRelatedReportsAdapter(getActivity(), items);
    mExpandableListView.setAdapter(mAdapter);
  }
}

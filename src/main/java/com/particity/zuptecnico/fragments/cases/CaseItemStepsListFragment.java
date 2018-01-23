package com.particity.zuptecnico.fragments.cases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.activities.cases.CaseItemDetailsActivity;
import com.particity.zuptecnico.adapters.CaseStepsAdapter;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.ui.ScrollLessListView;

public class CaseItemStepsListFragment extends Fragment {
    CaseStepsAdapter adapter;

    Case getItem() {
        return (Case) getArguments().getParcelable(CaseItemDetailsActivity.CASE);
    }

    Flow.Step[] getSteps(){
        return Flow.Step.toMyObjects(getArguments().getParcelableArray(CaseItemDetailsActivity.CASE_STEPS));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_case_item_details_steps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new CaseStepsAdapter(getActivity(), getItem(), getSteps());
        adapter.setListener((CaseStepsAdapter.OnCaseStepClickListener) getActivity());
        ScrollLessListView listView = (ScrollLessListView) view.findViewById(R.id.case_steps_listview);
        listView.setAdapter(this.adapter);
    }


}

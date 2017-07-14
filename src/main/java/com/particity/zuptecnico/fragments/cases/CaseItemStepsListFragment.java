package com.ntxdev.zuptecnico.fragments.cases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.activities.cases.CaseItemDetailsActivity;
import com.ntxdev.zuptecnico.adapters.CaseStepsAdapter;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.ui.ScrollLessListView;

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

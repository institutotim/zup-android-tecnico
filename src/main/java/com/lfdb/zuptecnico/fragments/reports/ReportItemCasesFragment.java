package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.adapters.ReportItemCasesAdapter;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.ui.ScrollLessListView;

/**
 * Created by Renan on 02/03/2016.
 */
public class ReportItemCasesFragment extends Fragment {
    Case[] cases;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_details_cases, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cases = Case.toMyObjects(getArguments().getParcelableArray("cases"));
        if (cases != null) {
            ReportItemCasesAdapter adapter = new ReportItemCasesAdapter(getActivity(), cases);
            ((ScrollLessListView) getView().findViewById(R.id.notification_listview)).setAdapter(adapter);
            hideLoading();
        } else {
            showLoading();
        }
    }

    void showLoading() {
        getView().findViewById(R.id.notificationinfo_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        getView().findViewById(R.id.notificationinfo_loading).setVisibility(View.GONE);
    }
}

package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.adapters.ReportItemHistoryAdapter;
import com.lfdb.zuptecnico.entities.ReportHistoryItem;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.ScrollLessListView;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemHistoryFragment extends Fragment {
    ReportItemHistoryAdapter adapter;

    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    ReportHistoryItem[] getHistory() {
        return ReportHistoryItem.toMyObjects(getArguments().getParcelableArray("history"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report_details_history, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadView(view);
    }

    public void refresh() {
        loadView(getView());
    }

    private void loadView(View view) {
        this.adapter = new ReportItemHistoryAdapter(getActivity(), getItem(), getHistory());
        ScrollLessListView listView = (ScrollLessListView) view.findViewById(R.id.report_history_listview);
        listView.setAdapter(this.adapter);
    }
}

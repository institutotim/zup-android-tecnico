package com.ntxdev.zuptecnico.fragments.reports;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.adapters.ReportStatusesAdapter;
import com.ntxdev.zuptecnico.entities.ReportCategory;

import java.util.List;

/**
 * Created by Renan on 21/10/2015.
 */
public class ReportStatusPickerDialog extends DialogFragment implements AdapterView.OnItemClickListener {
    View confirmButton;
    TextView headerTextView;
    int selectedStatusId = -1;
    int categoryId;
    ReportStatusesAdapter adapter;
    private OnReportStatusSetListener listener;

    public interface OnReportStatusSetListener {
        void onReportStatusSet(int selectedStatusId);
    }

    public static ReportStatusPickerDialog newInstance(int category, int status) {
        ReportStatusPickerDialog frag = new ReportStatusPickerDialog();
        Bundle args = new Bundle();
        args.putInt("category", category);
        args.putInt("status", status);
        frag.setArguments(args);
        return frag;
    }

    public void setListener(OnReportStatusSetListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_status_picker, container, false);
        confirmButton = view.findViewById(R.id.confirm);
        confirmButton.setVisibility(View.INVISIBLE);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        view.findViewById(R.id.confirm).setVisibility(View.VISIBLE);
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        headerTextView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null){
            if(getArguments().containsKey("category")) {
                categoryId = getArguments().getInt("category");
            }
            if(getArguments().containsKey("status")) {
                selectedStatusId = getArguments().getInt("status");
            }
        }
        updateAdapter(view);
    }

    protected void updateAdapter(View view) {
        if(adapter == null){
            adapter = new ReportStatusesAdapter(categoryId);
        }
        adapter.setSelectedStatusId(selectedStatusId);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ReportCategory.Status selectedStatus = adapter.getItem(i);
        adapter.setSelectedStatusId((Integer) selectedStatus.getId());
        toggleSelectedStatus(selectedStatus);
    }

    protected void toggleSelectedStatus(ReportCategory.Status reportStatus) {
        selectedStatusId = reportStatus.getId();
    }

    protected void confirm() {
        if(selectedStatusId == -1) {
            ZupApplication.toast(getView(), R.string.please_select_status).show();
            return;
        }
        if (listener != null) {
            listener.onReportStatusSet(selectedStatusId);
        }
        this.dismiss();
    }
}

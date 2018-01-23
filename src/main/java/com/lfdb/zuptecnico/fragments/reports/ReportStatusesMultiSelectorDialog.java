package com.particity.zuptecnico.fragments.reports;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.ReportStatusesMultiSelectAdapter;
import com.particity.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.List;

public class ReportStatusesMultiSelectorDialog extends ReportStatusPickerDialog implements View.OnClickListener {
    List<ReportCategory.Status> selectedStatuses;

    @Override
    public void onClick(View v) {
        ((ReportStatusesMultiSelectAdapter)adapter).clearSelection();
        selectedStatuses.clear();
        hideHeaderView();
    }

    public static ReportStatusesMultiSelectorDialog newInstance(String[] statuses) {
        ReportStatusesMultiSelectorDialog frag = new ReportStatusesMultiSelectorDialog();
        Bundle args = new Bundle();
        args.putStringArray("statusesId", statuses);
        frag.setArguments(args);
        return frag;
    }

    public interface OnReportStatusesSetListener {
        void onReportStatusSet(List<ReportCategory.Status> selectedStatuses);
    }

    OnReportStatusesSetListener listener;

    public ReportStatusesMultiSelectorDialog() {
        selectedStatuses = new ArrayList<ReportCategory.Status>();
    }

    public void setListener(OnReportStatusesSetListener listener) {
        this.listener = listener;
    }

    public int getSelectedStatusesCount() {
        return selectedStatuses.size();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null && getArguments().containsKey("statusesId")) {
            setSelectedStatuses(getArguments().getStringArray("statusesId"));
        }
    }

    private void setSelectedStatuses(String[] statusesId) {
        if (statusesId == null || statusesId.length == 0 || statusesId[0].isEmpty()) {
            return;
        }
        if(adapter == null){
            adapter = new ReportStatusesMultiSelectAdapter();
        }
        List<Integer> statusesList = new ArrayList<>();
        int size = statusesId.length;
        for (int index = 0; index < size; index++) {
            statusesList.add(Integer.parseInt(statusesId[index]));
        }

        ((ReportStatusesMultiSelectAdapter)adapter).setSelectedStatusesId(statusesList);
        selectedStatuses = ((ReportStatusesMultiSelectAdapter)adapter).getSelectedStatuses();
        showHeaderView(size);
    }

    @Override
    protected void updateAdapter(View view) {
        if(adapter == null) {
            adapter = new ReportStatusesMultiSelectAdapter();
        }
        if(headerTextView != null) {
            headerTextView.setOnClickListener(this);
        }
        super.updateAdapter(view);
        if (selectedStatuses != null) {
            ArrayList<Integer> statusesId = new ArrayList<Integer>();
            for (int index = 0; index < selectedStatuses.size(); index++) {
                statusesId.add(selectedStatuses.get(index).getId());
            }
            if (adapter != null) {
                ((ReportStatusesMultiSelectAdapter)adapter).setSelectedStatusesId(statusesId);
            }
            if (statusesId.size() > 0) {
                showHeaderView(statusesId.size());
            } else {
                hideHeaderView();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        super.onItemClick(adapterView, view, i, l);
        if (((ReportStatusesMultiSelectAdapter)adapter).getSelectedStatusesCount() > 0) {
            showHeaderView(((ReportStatusesMultiSelectAdapter)adapter).getSelectedStatusesCount());
        } else {
            hideHeaderView();
        }
    }

    protected void showHeaderView(int selectedUsersCount) {
        if (headerTextView != null) {
            try {
                ColorStateList colors = ContextCompat.getColorStateList(getActivity(), R.drawable.button_dialog_title);
                headerTextView.setTextColor(colors);
            } catch (Exception e) {
                headerTextView.setTextColor(getResources().getColor(R.color.zupblue));
            }
            headerTextView.setClickable(true);
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + selectedUsersCount + ")");
        }
    }

    protected void hideHeaderView() {
        if (headerTextView != null) {
            headerTextView.setVisibility(View.VISIBLE);
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + 0 + ")");
            headerTextView.setTextColor(getResources().getColor(R.color.report_item_selecting));
            headerTextView.setClickable(false);
        }
    }

    protected void toggleSelectedStatus(ReportCategory.Status reportStatus) {
        if (selectedStatuses.contains(reportStatus)) {
            selectedStatuses.remove(reportStatus);
        } else {
            selectedStatuses.add(reportStatus);
        }
    }

    protected void confirm() {
        if (listener != null)
            listener.onReportStatusSet(selectedStatuses);
        this.dismiss();
    }
}

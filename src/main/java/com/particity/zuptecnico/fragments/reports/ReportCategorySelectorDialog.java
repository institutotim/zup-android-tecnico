package com.particity.zuptecnico.fragments.reports;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.ReportCategoriesAdapter;
import com.particity.zuptecnico.entities.ReportCategory;

/**
 * Created by igorlira on 7/20/15.
 */
public class ReportCategorySelectorDialog extends DialogFragment implements AdapterView.OnItemClickListener {
    View confirmButton;
    ListView listView;

    public interface OnReportCategorySetListener {
        void onReportCategorySet(int categoryId);
    }

    public boolean isEdit() {
        return getArguments() != null && getArguments().getBoolean("isEdit");
    }

    OnReportCategorySetListener listener;
    ReportCategoriesAdapter adapter;

    public void setListener(OnReportCategorySetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_createreport, null, false);
        builder.setView(view);
        initView(view);
        return builder.create();
    }

    void initView(View view){
        listView = (ListView) view.findViewById(R.id.listView);
        confirmButton = view.findViewById(R.id.confirm);
        confirmButton.setVisibility(View.INVISIBLE);
        if(adapter == null) {
            adapter = new ReportCategoriesAdapter(isEdit());
        }
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm(adapter.getSelectedItemId());
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        ReportCategory category = adapter.getItem(i);
        adapter.clickCategory(category);

        if (adapter.getSelectedItemId() >= 0) {
            confirmButton.setVisibility(View.VISIBLE);
        }
        if (category.subcategories != null && category.subcategories.length > 0) {
            int newIndex = adapter.getItemIndex(category);
            listView.smoothScrollToPositionFromTop(newIndex, 0);
        }
    }


    void confirm(int id) {
        if (this.listener != null)
            this.listener.onReportCategorySet(id);

        dismiss();
    }
}

package com.lfdb.zuptecnico.fragments.cases;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.adapters.ReportsPickerAdapter;
import com.lfdb.zuptecnico.entities.ReportItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 04/01/2016.
 */
public class ReportPickerDialog extends DialogFragment implements AdapterView.OnItemClickListener, ReportsPickerAdapter.ReportsPickerAdapterListener {
    ReportsPickerAdapter adapter;
    private EditText searchText;
    private OnReportsMultiSelectPickedListener multiSelectPickedListener;
    List<ReportItem> selectedReports;
    List<Integer> selectedReportsId;
    TextView headerTextView;

    public void onReportsLoaded() {
        if (selectedReportsId != null) {
            adapter.setSelectedReportsId(selectedReportsId);
            selectedReports = adapter.getSelectedReports();
            showHeaderView(selectedReportsId.size());
        }
    }

    public void setSelectedReports(Integer[] reportsId) {
        if (reportsId == null || reportsId.length == 0) {
            return;
        }
        List<Integer> reportsList = new ArrayList<>();
        for (int index = 0; index < reportsId.length; index++) {
            reportsList.add(reportsId[index]);
        }
        selectedReportsId = reportsList;
    }

    public interface OnReportsMultiSelectPickedListener {
        void onReportsPicked(List<ReportItem> users);
    }

    public void setListener(OnReportsMultiSelectPickedListener listener) {
        multiSelectPickedListener = listener;
    }

    private void updateAdapter() {
        if (selectedReports != null && selectedReports.size() > 0) {
            selectedReportsId = new ArrayList<>();
            for (int index = 0; index < selectedReports.size(); index++) {
                selectedReportsId.add(selectedReports.get(index).id);
            }
        } else if (selectedReportsId == null) {
            selectedReportsId = new ArrayList<>();
        }
        if (adapter != null) {
            adapter.setSelectedReportsId(selectedReportsId);
        }
        if (selectedReportsId.size() > 0) {
            showHeaderView(selectedReportsId.size());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_userpicker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        ((TextView) view.findViewById(R.id.textView31)).setText("Selecione o relato");
        ((EditText) view.findViewById(R.id.search_edit)).setText("Buscar relato");
        hideHeaderView();
        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clearSelection();
                selectedReports.clear();
                hideHeaderView();
            }
        });
        super.onViewCreated(view, savedInstanceState);

        hideConfirmButton();
        ListView listView = (ListView) view.findViewById(R.id.listView);

        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);

        searchText = (EditText) view.findViewById(R.id.search_edit);
        searchText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchTextChanged(charSequence.toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        loadAdapter(listView);
        if (selectedReports == null)
            selectedReports = new ArrayList<>();
        showConfirmButton();
    }

    void loadAdapter(ListView listView) {
        if (adapter == null) {
            adapter = new ReportsPickerAdapter(this.getActivity());
            adapter.setListener(this);
        }
        listView.setAdapter(adapter);
        adapter.load();
        updateAdapter();
    }

    private void showHeaderView(int selectedUsersCount) {
        if (!isAdded()) {
            return;
        }
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

    private void hideHeaderView() {
        if (headerTextView != null) {
            headerTextView.setVisibility(View.VISIBLE);
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + 0 + ")");
            headerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.report_item_selecting));
            headerTextView.setClickable(false);
        }
    }

    void showConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }

    void hideConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
    }

    void searchTextChanged(String newQuery) {
        adapter.setQuery(newQuery);
    }

    void confirm() {
        if (multiSelectPickedListener != null)
            multiSelectPickedListener.onReportsPicked(selectedReports);
        this.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (searchText != null && isAdded()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ReportItem selectedReport = adapter.getItem(i);
        toggleSelectedReport(selectedReport);
        adapter.setSelectedReportId((Integer) selectedReport.id);
        if (adapter.getSelectedReportsCount() > 0) {
            showHeaderView(adapter.getSelectedReportsCount());
        } else {
            hideHeaderView();
        }

    }

    private void toggleSelectedReport(ReportItem report) {
        if (selectedReports.contains(report)) {
            selectedReports.remove(report);
        } else {
            selectedReports.add(report);
        }
    }
}

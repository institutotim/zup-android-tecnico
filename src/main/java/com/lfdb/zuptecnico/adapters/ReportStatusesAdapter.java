package com.lfdb.zuptecnico.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportCategory;
import com.lfdb.zuptecnico.entities.ReportCategory.Status;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Renan on 25/08/2015.
 */
public class ReportStatusesAdapter extends BaseAdapter {
    private int selectedStatusId;
    protected Status[] statusList;
    private int categoryId;

    public ReportStatusesAdapter(int categoryId) {
        this.categoryId = categoryId;
        updateList();
    }

    protected void updateList() {
        Set<Status> statusesList = new HashSet<>();
        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);
        if (category == null || category.statuses == null) {
            return;
        }
        for (int auxIndex = 0; auxIndex < category.statuses.length; auxIndex++) {
            statusesList.add(category.statuses[auxIndex]);
        }
        statusList = new Status[statusesList.size()];
        Iterator<Status> iterator = statusesList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            statusList[i] = iterator.next();
            i++;
        }
    }

    public void setSelectedStatusId(Integer selectedUserId) {
        this.selectedStatusId = selectedUserId;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return statusList == null ? 0 : statusList.length;
    }

    @Override
    public Status getItem(int position) {
        return statusList == null ? null : statusList[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position) == null ? 0 : getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.statuses_list_item, parent, false);
        Status status = getItem(position);
        TextView statusName = (TextView) root.findViewById(R.id.status_name);
        CheckBox checkBox = (CheckBox) root.findViewById(R.id.status_selected_checkbox);
        View checked = root.findViewById(R.id.status_selected);
        if (this instanceof ReportStatusesMultiSelectAdapter) {
            checked.setVisibility(View.GONE);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(isSelected(status.getId()) ? true : false);
        } else {
            checked.setVisibility(isSelected(status.getId()) ? View.VISIBLE : View.GONE);
            checkBox.setVisibility(View.GONE);
        }
        statusName.setText(status.getTitle());
        return root;
    }

    protected boolean isSelected(int id) {
        return selectedStatusId == id;
    }

}

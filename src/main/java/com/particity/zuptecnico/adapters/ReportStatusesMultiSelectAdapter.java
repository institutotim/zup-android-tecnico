package com.ntxdev.zuptecnico.adapters;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Renan on 21/10/2015.
 */
public class ReportStatusesMultiSelectAdapter extends ReportStatusesAdapter {
    private List<Integer> selectedStatusesId;

    public ReportStatusesMultiSelectAdapter() {
        super(0);
        selectedStatusesId = new ArrayList<>();
    }

    @Override
    protected void updateList() {
        Set<ReportCategory.Status> statusesList = new HashSet<>();
        ReportCategory[] categoryList = Zup.getInstance().getReportCategoryService().getReportCategories();
        if (categoryList != null) {
            for (int index = 0; index < categoryList.length; index++) {
                ReportCategory category = categoryList[index];
                statusesList.addAll(getCategoryStatuses(category));
            }
        }
        statusList = new ReportCategory.Status[statusesList.size()];
        Iterator<ReportCategory.Status> iterator = statusesList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            statusList[i] = iterator.next();
            i++;
        }
    }

    private List<ReportCategory.Status> getCategoryStatuses(ReportCategory category) {
        List<ReportCategory.Status> statuses = new ArrayList<>();
        if (category == null) {
            return statuses;
        }
        if (category.subcategories != null) {
            for (int index = 0; index < category.subcategories.length; index++) {
                statuses.addAll(getCategoryStatuses(category.subcategories[index]));
            }
        }
        if (category.statuses != null) {
            for (int index = 0; index < category.statuses.length; index++) {
                statuses.add(category.statuses[index]);
            }
        }
        return statuses;
    }

    public void clearSelection() {
        selectedStatusesId.clear();
        notifyDataSetInvalidated();
    }

    public void setSelectedStatusesId(List<Integer> usersId) {
        selectedStatusesId.clear();
        selectedStatusesId.addAll(usersId);
        notifyDataSetInvalidated();
    }

    public List<ReportCategory.Status> getSelectedStatuses() {
        List<ReportCategory.Status> statusesList = new ArrayList<ReportCategory.Status>();
        if (selectedStatusesId != null) {
            for (int index = 0; index < selectedStatusesId.size(); index++) {
                for (int j = 0; j < getCount(); j++) {
                    if (getItemId(j) == selectedStatusesId.get(index)) {
                        statusesList.add(getItem(j));
                        break;
                    }
                }
            }
        }
        return statusesList;
    }

    @Override
    public void setSelectedStatusId(Integer selectedUserId) {
        super.setSelectedStatusId(selectedUserId);
        if (selectedStatusesId.contains(selectedUserId)) {
            selectedStatusesId.remove(selectedUserId);
        } else {
            selectedStatusesId.add(selectedUserId);
        }
        notifyDataSetInvalidated();
    }

    @Override
    protected boolean isSelected(int id) {
        return selectedStatusesId.contains(id);
    }

    public int getSelectedStatusesCount() {
        return selectedStatusesId.size();
    }

    public List<Integer> getSelectedStatusesId() {
        return selectedStatusesId;
    }
}

package com.lfdb.zuptecnico.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 24/08/2015.
 */
public class ReportCategoriesMultiSelectAdapter extends ReportCategoriesAdapter {
    private List<Integer> selectedCategoriesId;

    public ReportCategoriesMultiSelectAdapter() {
        super(false);
        selectedCategoriesId = new ArrayList<Integer>();
    }

    @Override
    void filterByPermissionsToCreate() {
        ReportCategory[] oldArray = this.categoryList;

        List<ReportCategory> newArray = new ArrayList<>();
        for (ReportCategory category : oldArray) {
            if (Zup.getInstance().getAccess().canViewReportCategory(category.id)) {
                newArray.add(category);
            }
        }

        this.categoryList = new ReportCategory[newArray.size()];
        newArray.toArray(this.categoryList);
    }

    public List<Integer> getSelectedCategoriesId() {
        return selectedCategoriesId;
    }

    public void clearSelection() {
        selectedCategoriesId.clear();
        notifyDataSetInvalidated();
    }

    public void setSelectedStatusesId(List<Integer> categoriesId) {
        selectedCategoriesId.clear();
        selectedCategoriesId.addAll(categoriesId);
        notifyDataSetInvalidated();
    }

    public List<ReportCategory> getSelectedCategories(){
        List<ReportCategory> selectedCategoriesList = new ArrayList<ReportCategory>();
        if(selectedCategoriesId != null) {
            for (int index = 0; index < selectedCategoriesId.size(); index++) {
                for (int j = 0; j < getCount(); j++) {
                    if (getItemId(j) == selectedCategoriesId.get(index)) {
                        selectedCategoriesList.add(getItem(j));
                        break;
                    }
                }
            }
        }
        return selectedCategoriesList;
    }

    public void setSelectedCategoriesId(List<Integer> categoriesId) {
        selectedCategoriesId.clear();
        selectedCategoriesId.addAll(categoriesId);
        notifyDataSetInvalidated();
    }

    public void setSelectedCategoryId(Integer selectedCategoryId) {
        if (selectedCategoriesId.contains(selectedCategoryId)) {
            selectedCategoriesId.remove(selectedCategoryId);
        } else {
            selectedCategoriesId.add(selectedCategoryId);
        }
        notifyDataSetInvalidated();
    }

    public void removeSelectedCategoryId(Integer selectedCategoryId) {
        selectedCategoriesId.remove(selectedCategoryId);
        notifyDataSetInvalidated();
    }

    public void insertSelectedCategoryId(Integer selectedCategoryId) {
        if (!selectedCategoriesId.contains(selectedCategoryId)) {
            selectedCategoriesId.add(selectedCategoryId);
            notifyDataSetInvalidated();
        }
    }

    public int getSelectedCategoriesCount() {
        return selectedCategoriesId.size();
    }

    @Override
    public ReportCategory getItem(int index) {
        int position = 0;
        for (int i = 0; i < categoryList.length; i++) {
            if (position == index) {
                expandedCategory = categoryList[i];
                break;
            } else if (categoryList[i].subcategories != null) {
                int subCategoriesLength = categoryList[i].subcategories.length;
                if ((position + subCategoriesLength) < index) {
                    position += subCategoriesLength + 1;
                } else if ((position + subCategoriesLength) >= index) {
                    expandedCategory = categoryList[i].subcategories[(index - position - 1)];
                    break;
                }
            } else {
                position++;
            }
        }
        return expandedCategory;
    }

    @Override
    public int getCount() {
        int count = this.categoryList.length;
        for (int index = 0; index < categoryList.length; index++) {
            if (categoryList[index].subcategories != null) {
                count += categoryList[index].subcategories.length;
            }
        }
        return count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root = super.getView(position, convertView, parent);
        ReportCategory category = getItem(position);
        if (root.findViewById(R.id.selected) != null) {
            root.findViewById(R.id.selected).setVisibility(View.INVISIBLE);
            CheckBox checkBox = (CheckBox) root.findViewById(R.id.category_selected_checkbox);
            checkBox.setVisibility(View.VISIBLE);
            //int parentCategoryId = (category.parent_id == null || category.parent_id == 0) ? 0 : category.parent_id;
            if (selectedCategoriesId.contains(category.id)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }


        }
        return root;
    }
}

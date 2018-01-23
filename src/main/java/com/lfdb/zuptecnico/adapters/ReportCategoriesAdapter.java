package com.lfdb.zuptecnico.adapters;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportCategory;
import com.lfdb.zuptecnico.ui.WebImageView;

import java.util.ArrayList;
import java.util.List;

public class ReportCategoriesAdapter extends BaseAdapter {
    protected ReportCategory[] categoryList;
    private SparseArray<View> viewCache;
    protected ReportCategory expandedCategory = null;
    private int selectedCategoryId = -1;
    private boolean isEdit = false;

    public ReportCategoriesAdapter(boolean isEdit) {
        this.viewCache = new SparseArray<>();
        this.categoryList = Zup.getInstance().getReportCategoryService().getReportCategories();
        this.isEdit = isEdit;

        this.filterByPermissionsToCreate();
    }

    void filterByPermissionsToCreate() {
        ReportCategory[] oldArray = this.categoryList;

        List<ReportCategory> newArray = new ArrayList<>();
        for (ReportCategory category : oldArray) {
            if (!isEdit && Zup.getInstance().getAccess().canCreateReportItem(category.id)) {
                newArray.add(category);
            } else if (isEdit && Zup.getInstance().getAccess().canEditReportItem(category.id)) {
                newArray.add(category);
            }
        }

        this.categoryList = new ReportCategory[newArray.size()];
        newArray.toArray(this.categoryList);
    }

    public int getSelectedItemId() {
        return selectedCategoryId;
    }

    @Override
    public int getCount() {
        int count = this.categoryList.length;
        if (expandedCategory != null && expandedCategory.subcategories != null)
            count += expandedCategory.subcategories.length;

        return count;
    }

    @Override
    public ReportCategory getItem(int index) {
        if (expandedCategory == null || expandedCategory.subcategories == null)
            return categoryList[index];

        int baseIndex = 0;
        for (int i = 0; i < categoryList.length; i++) {
            if (categoryList[i].id == expandedCategory.id) {
                baseIndex = i;
                break;
            }
        }

        if (index <= baseIndex)
            return categoryList[index];

        int offsetIndex = index - baseIndex - 1;
        if (offsetIndex < expandedCategory.subcategories.length)
            return expandedCategory.subcategories[offsetIndex];
        else {
            return categoryList[index - expandedCategory.subcategories.length];
        }
    }

    public void expandCategory(int id) {
        ReportCategory oldSelected = expandedCategory;

        for (int i = 0; i < categoryList.length; i++) {
            if (categoryList[i].id == id) {
                expandedCategory = categoryList[i];
                break;
            }
        }

        if (expandedCategory != oldSelected)
            notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        ReportCategory category = getItem(i);
        if (viewCache.get(category.id, null) != null) {
            View root = viewCache.get(category.id);

            View viewSelected = root.findViewById(R.id.selected);
            viewSelected.setVisibility(selectedCategoryId == category.id ? View.VISIBLE : View.GONE);

            return root;
        }

        View root = inflater.inflate(R.layout.report_category_list_item, viewGroup, false);
        boolean isRoot = category.parent_id == null || category.parent_id == 0;

        TextView txtTitle = (TextView) root.findViewById(R.id.category_title);
        WebImageView imageView = (WebImageView) root.findViewById(R.id.category_icon);
        View dividerCat = root.findViewById(R.id.divider_category);
        View dividerSub = root.findViewById(R.id.divider_subcategory);
        View viewSelected = root.findViewById(R.id.selected);

        txtTitle.setText(category.title);

        if (isRoot)
            category.loadImageInto(imageView);

        dividerCat.setVisibility(isRoot ? View.VISIBLE : View.GONE);
        dividerSub.setVisibility(!isRoot ? View.VISIBLE : View.GONE);
        viewSelected.setVisibility(selectedCategoryId == category.id ? View.VISIBLE : View.INVISIBLE);

        viewCache.put(category.id, root);

        return root;
    }

    public int getItemIndex(ReportCategory item) {
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).id == item.id) {
                return i;
            }
        }

        return -1;
    }

    void selectCategory(int id) {
        int oldSelected = selectedCategoryId;
        selectedCategoryId = id;

        if (selectedCategoryId != oldSelected)
            notifyDataSetChanged();
    }

    public void clickCategory(ReportCategory category) {
        if (expandedCategory == category) {
            selectCategory(category.id);
        } else if (category.subcategories != null && category.subcategories.length > 0) {
            expandCategory(category.id);
        } else {
            selectCategory(category.id);
        }
    }
}

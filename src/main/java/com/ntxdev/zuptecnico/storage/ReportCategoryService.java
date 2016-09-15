package com.ntxdev.zuptecnico.storage;

import android.graphics.Bitmap;

import com.ntxdev.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportCategoryService extends BaseService {

    public ReportCategoryService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("report_categories");
    }

    public ReportCategory getReportCategory(int id) {
        ReportCategory category = getObject("report_category_" + id, ReportCategory.class);
        if(category != null && category.subcategories_ids != null) {
            category.subcategories = new ReportCategory[category.subcategories_ids.length];
            for(int i = 0; i < category.subcategories_ids.length; i++) {
                category.subcategories[i] = getReportCategory(category.subcategories_ids[i]);
            }
        }

        return category;
    }

    public ReportCategory[] getReportCategories() {
        Integer[] values = getObjectArray("report_categories", Integer.class);
        if(values == null)
            return new ReportCategory[0];

        List<ReportCategory> result = new ArrayList<>();

        for(int i = 0; i < values.length; i++)
        {
            Integer categoryId = values[i];
            ReportCategory category = getReportCategory(categoryId);
            if (category == null) {
                continue;
            }
            if(category.parent_id == null || category.parent_id == 0)
                result.add(category);
        }

        ReportCategory[] resultArray = new ReportCategory[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    public void addReportCategory(ReportCategory category) {
        List<Integer> ids = getObjectList("report_categories", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(category.id)) {
            ids.add(category.id);
            setList("report_categories", ids);
        }

        saveReportCategory(category, null);
    }

    // Dismember and save subcategories
    void saveReportCategory(ReportCategory category, List<Integer> idArray) {
        ReportCategory[] subCategories = category.subcategories;
        category.saveImageIntoCache(mManager.getContext());
        category.saveMarkerIntoCache(mManager.getContext());
        category.subcategories = null;
        if(subCategories != null)
        {
            category.subcategories_ids = new Integer[subCategories.length];
            for(int i = 0; i < subCategories.length; i++) {
                category.subcategories_ids[i] = subCategories[i].id;
                saveReportCategory(subCategories[i], idArray);
            }
        }

        if(idArray != null)
            idArray.add(category.id);

        setObject("report_category_" + category.id, category);
    }

    public void setReportCategories(ReportCategory[] categories) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(int i = 0; i < categories.length; i++) {
            saveReportCategory(categories[i], ids);
        }
        
        setList("report_categories", ids);
    }

    public void setReportCategoryMarker(int id, Bitmap image) {
        setBitmap("report_category_marker_" + id, image);
    }

    public Bitmap getReportCategoryMarker(int id) {
        return getBitmap("report_category_marker_" + id);
    }
}

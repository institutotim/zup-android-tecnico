package com.lfdb.zuptecnico.storage;

import com.lfdb.zuptecnico.entities.InventoryCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 22/01/2016.
 */
public class InventoryCategoryService extends BaseService {

    public InventoryCategoryService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("inventory_categories");
    }

    public InventoryCategory getInventoryCategory(int id) {
        InventoryCategory category = getObject("inventory_category_" + id, InventoryCategory.class);
        return category;
    }

    public InventoryCategory[] getInventoryCategories() {
        Integer[] values = getObjectArray("inventory_categories", Integer.class);
        if (values == null)
            return new InventoryCategory[0];

        List<InventoryCategory> result = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            Integer categoryId = values[i];
            InventoryCategory category = getInventoryCategory(categoryId);
            if (category != null) {
                result.add(category);
            }
        }

        InventoryCategory[] resultArray = new InventoryCategory[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    public void addInventoryCategory(InventoryCategory category) {
        List<Integer> ids = getObjectList("inventory_categories", Integer.class);
        if (ids == null)
            ids = new ArrayList<>();

        if (!ids.contains(category.id)) {
            ids.add(category.id);
            setList("inventory_categories", ids);
        }

        saveInventoryCategory(category, null);
    }

    void saveInventoryCategory(InventoryCategory category, List<Integer> idArray) {
        category.saveImageIntoCache(mManager.getContext());
        category.saveMarkerIntoCache(mManager.getContext());
        if (idArray != null)
            idArray.add(category.id);

        setObject("inventory_category_" + category.id, category);
    }

    public void setInventoryCategories(InventoryCategory[] categories) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < categories.length; i++) {
            saveInventoryCategory(categories[i], ids);
        }

        setList("inventory_categories", ids);
    }
}

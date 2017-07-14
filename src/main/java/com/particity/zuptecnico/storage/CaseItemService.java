package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.collections.CaseHistoryCollection;
import com.ntxdev.zuptecnico.entities.collections.CaseHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class CaseItemService extends BaseService {
    public CaseItemService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("case_items");
    }

    public int getCaseItemCount() {
        List<Integer> ids = getObjectList("case_items", Integer.class);
        if (ids == null)
            return 0;

        return ids.size();
    }

    public boolean hasCaseItem(int id) {
        List<Integer> ids = getObjectList("case_items", Integer.class);
        if (ids == null)
            return false;

        return ids.contains(id);
    }

    public Case getCaseItemAtIndex(int index) {
        List<Integer> ids = getObjectList("case_items", Integer.class);
        if (ids == null || index >= ids.size())
            return null;

        int id = ids.get(index);
        return getCaseItem(id);
    }

    public Case getCaseItem(int id) {
        Case item = getObject("case_item_" + id, Case.class);
        return item;
    }

    public void addCaseItem(Case item) {
        List<Integer> ids = getObjectList("case_items", Integer.class);
        if (ids == null)
            ids = new ArrayList<>();

        if (!ids.contains(item.id)) {
            ids.add(item.id);
            setList("case_items", ids);
        }

        saveCaseItem(item);
    }

    public void deleteCaseItem(int id) {
        deleteObject("case_item_" + id);
        List<Integer> ids = getObjectList("case_items", Integer.class);
        if (ids == null)
            return;

        if (ids.contains(id)) {
            ids.remove((Integer) id);
            setList("case_items", ids);
        }
    }

    private void saveCaseItem(Case item) {
        setObject("case_item_" + item.id, item);
    }

    public void setCaseItemHistory(int itemId, CaseHistoryItem[] histories) {
        CaseHistoryCollection collection = new CaseHistoryCollection();
        collection.histories = histories;

        setObject("case_item_" + itemId + "_histories", collection);
    }

    public CaseHistoryItem[] getCaseItemHistory(int itemId) {
        CaseHistoryCollection collection = getObject("case_item_" + itemId + "_histories", CaseHistoryCollection.class);
        if (collection == null)
            return null;

        return collection.histories;
    }
}
